package network;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonSyntaxException;

import log.Logger;
import network.protocol.ClientSetTargetCommand;
import network.protocol.RobotWireProtocol;
import network.protocol.ServerErrorEvent;
import network.protocol.ServerStateEvent;
import network.protocol.ServerWelcomeEvent;
import network.protocol.WireMessage;

public class RobotGameClient implements Closeable
{
    private static final String LOG_SOURCE = "network.RobotGameClient";
    private final String host;
    private final int port;
    private final RobotGameClientListener listener;
    private final Object writeLock = new Object();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private Socket socket;
    private BufferedWriter writer;
    private Thread readerThread;
    private volatile boolean disconnectNotified;

    public RobotGameClient(String host, int port, RobotGameClientListener listener)
    {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public void connect() throws IOException
    {
        if (connected.get())
        {
            Logger.debug(LOG_SOURCE, "connect_skipped", "Client already connected.");
            return;
        }
        Logger.info(LOG_SOURCE, "connect", "Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        connected.set(true);
        disconnectNotified = false;

        readerThread = new Thread(() -> readLoop(reader), "robot-client-reader-" + host + "-" + port);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public boolean isConnected()
    {
        return connected.get();
    }

    public boolean sendSetTarget(Point point)
    {
        if (!connected.get())
        {
            Logger.warn(LOG_SOURCE, "send_skipped", "Cannot send target while disconnected.");
            return false;
        }
        String line = RobotWireProtocol.encodeMessage(
                RobotWireProtocol.TYPE_CLIENT_SET_TARGET,
                new ClientSetTargetCommand(point.x, point.y));
        try
        {
            synchronized (writeLock)
            {
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
            return true;
        }
        catch (IOException e)
        {
            Logger.error(LOG_SOURCE, "write_failed", "Network write error.", e);
            notifyDisconnect("Connection lost while sending command.");
            closeQuietly();
            return false;
        }
    }

    @Override
    public void close()
    {
        closeQuietly();
    }

    private void readLoop(BufferedReader reader)
    {
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                handleIncomingMessage(line);
            }
            notifyDisconnect("Disconnected from server.");
        }
        catch (IOException e)
        {
            Logger.error(LOG_SOURCE, "read_failed", "Connection error while reading server stream.", e);
            notifyDisconnect("Connection error: " + e.getMessage());
        }
        finally
        {
            closeQuietly();
        }
    }

    private void handleIncomingMessage(String line)
    {
        try
        {
            WireMessage message = RobotWireProtocol.decodeMessage(line);
            if (message == null || message.getType() == null)
            {
                return;
            }
            if (RobotWireProtocol.TYPE_SERVER_WELCOME.equals(message.getType()))
            {
                ServerWelcomeEvent event = RobotWireProtocol.decodePayload(message, ServerWelcomeEvent.class);
                listener.onWelcome(event);
                return;
            }
            if (RobotWireProtocol.TYPE_SERVER_STATE.equals(message.getType()))
            {
                ServerStateEvent event = RobotWireProtocol.decodePayload(message, ServerStateEvent.class);
                listener.onState(event);
                return;
            }
            if (RobotWireProtocol.TYPE_SERVER_ERROR.equals(message.getType()))
            {
                ServerErrorEvent event = RobotWireProtocol.decodePayload(message, ServerErrorEvent.class);
                listener.onServerError(event);
            }
        }
        catch (JsonSyntaxException e)
        {
            Logger.error(LOG_SOURCE, "bad_json", "Invalid message from server.", e);
        }
    }

    private void notifyDisconnect(String message)
    {
        if (disconnectNotified)
        {
            return;
        }
        disconnectNotified = true;
        Logger.warn(LOG_SOURCE, "disconnected", message);
        listener.onDisconnected(message);
    }

    private void closeQuietly()
    {
        connected.set(false);
        closeSocket(socket);
        socket = null;
        writer = null;
    }

    private static void closeSocket(Socket socket)
    {
        if (socket == null)
        {
            return;
        }
        try
        {
            socket.close();
        }
        catch (IOException ignored)
        {
            // Nothing to do on shutdown.
        }
    }
}
