package network;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonSyntaxException;

import log.Logger;
import model.RobotModel;
import model.RobotState;
import network.protocol.ClientSetTargetCommand;
import network.protocol.RobotWireProtocol;
import network.protocol.ServerErrorEvent;
import network.protocol.ServerStateEvent;
import network.protocol.ServerWelcomeEvent;
import network.protocol.WireMessage;

public class RobotGameServer implements Closeable
{
    private static final String LOG_SOURCE = "network.RobotGameServer";
    private static final long TICK_MS = 10L;

    private final int port;
    private final RobotModel model = new RobotModel();
    private final List<ClientConnection> clients = new CopyOnWriteArrayList<ClientConnection>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong serverTick = new AtomicLong(0L);
    private final Object modelLock = new Object();

    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private Thread acceptThread;
    private Timer timer;

    public RobotGameServer(int port)
    {
        this.port = port;
    }

    public synchronized void start() throws IOException
    {
        if (running.get())
        {
            Logger.debug(LOG_SOURCE, "start_skipped", "Server already running.");
            return;
        }
        serverSocket = new ServerSocket(port);
        clientExecutor = Executors.newCachedThreadPool();
        running.set(true);
        startSimulationLoop();
        startAcceptLoop();
        Logger.info(LOG_SOURCE, "start", "Server started on port " + getPort());
    }

    public int getPort()
    {
        if (serverSocket != null)
        {
            return serverSocket.getLocalPort();
        }
        return port;
    }

    public boolean isRunning()
    {
        return running.get();
    }

    @Override
    public synchronized void close()
    {
        stop();
    }

    public synchronized void stop()
    {
        if (!running.getAndSet(false))
        {
            return;
        }
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
        closeServerSocket(serverSocket);
        serverSocket = null;

        for (ClientConnection client : clients)
        {
            client.close();
        }
        clients.clear();

        if (clientExecutor != null)
        {
            clientExecutor.shutdownNow();
            clientExecutor = null;
        }
        Logger.info(LOG_SOURCE, "stop", "Server stopped.");
    }

    private void startSimulationLoop()
    {
        timer = new Timer("robot-server-timer", true);
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                tickAndBroadcast();
            }
        }, 0L, TICK_MS);
    }

    private void startAcceptLoop()
    {
        acceptThread = new Thread(() -> {
            while (running.get())
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    ClientConnection client = new ClientConnection(UUID.randomUUID().toString(), socket);
                    clients.add(client);
                    Logger.info(LOG_SOURCE, "client_connected", client.id);
                    sendWelcome(client);
                    sendStateTo(client);
                    clientExecutor.submit(() -> handleClient(client));
                }
                catch (IOException e)
                {
                    if (running.get())
                    {
                        Logger.error(LOG_SOURCE, "accept_failed", "Server accept error.", e);
                    }
                }
            }
        }, "robot-server-accept-" + port);
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void handleClient(ClientConnection client)
    {
        try
        {
            String line;
            while (running.get() && (line = client.reader.readLine()) != null)
            {
                handleClientMessage(client, line);
            }
        }
        catch (IOException e)
        {
            Logger.error(LOG_SOURCE, "client_read_failed", "Client read error for " + client.id, e);
        }
        finally
        {
            removeClient(client);
        }
    }

    private void handleClientMessage(ClientConnection client, String line)
    {
        try
        {
            WireMessage message = RobotWireProtocol.decodeMessage(line);
            if (message == null || message.getType() == null)
            {
                sendError(client, "bad_request", "Malformed message.");
                return;
            }
            if (RobotWireProtocol.TYPE_CLIENT_SET_TARGET.equals(message.getType()))
            {
                ClientSetTargetCommand command = RobotWireProtocol.decodePayload(message, ClientSetTargetCommand.class);
                if (command == null)
                {
                    sendError(client, "bad_request", "Command payload is missing.");
                    return;
                }
                synchronized (modelLock)
                {
                    model.setTargetPosition(new Point(command.getX(), command.getY()));
                }
                Logger.debug(LOG_SOURCE, "set_target", "client=" + client.id + ", x=" + command.getX() + ", y=" + command.getY());
                broadcastStateSnapshot();
                return;
            }
            sendError(client, "unknown_type", "Unsupported message type: " + message.getType());
        }
        catch (JsonSyntaxException e)
        {
            Logger.warn(LOG_SOURCE, "bad_json", "Invalid JSON from client " + client.id);
            sendError(client, "bad_json", "Invalid JSON message.");
        }
    }

    private void tickAndBroadcast()
    {
        synchronized (modelLock)
        {
            model.update(TICK_MS);
        }
        serverTick.incrementAndGet();
        broadcastStateSnapshot();
    }

    private void broadcastStateSnapshot()
    {
        RobotState state;
        synchronized (modelLock)
        {
            state = model.getState();
        }
        long tick = serverTick.get();
        ServerStateEvent event = ServerStateEvent.fromState(state, tick);
        String payload = RobotWireProtocol.encodeMessage(RobotWireProtocol.TYPE_SERVER_STATE, event);
        for (ClientConnection client : clients)
        {
            if (!sendRaw(client, payload))
            {
                removeClient(client);
            }
        }
    }

    private void sendWelcome(ClientConnection client)
    {
        ServerWelcomeEvent event = new ServerWelcomeEvent(client.id);
        sendRaw(client, RobotWireProtocol.encodeMessage(RobotWireProtocol.TYPE_SERVER_WELCOME, event));
    }

    private void sendStateTo(ClientConnection client)
    {
        RobotState state;
        synchronized (modelLock)
        {
            state = model.getState();
        }
        long tick = serverTick.get();
        ServerStateEvent event = ServerStateEvent.fromState(state, tick);
        sendRaw(client, RobotWireProtocol.encodeMessage(RobotWireProtocol.TYPE_SERVER_STATE, event));
    }

    private void sendError(ClientConnection client, String code, String message)
    {
        ServerErrorEvent error = new ServerErrorEvent(code, message);
        sendRaw(client, RobotWireProtocol.encodeMessage(RobotWireProtocol.TYPE_SERVER_ERROR, error));
    }

    private boolean sendRaw(ClientConnection client, String payload)
    {
        try
        {
            synchronized (client.writeLock)
            {
                client.writer.write(payload);
                client.writer.newLine();
                client.writer.flush();
            }
            return true;
        }
        catch (IOException e)
        {
            Logger.error(LOG_SOURCE, "client_write_failed", "Client write error for " + client.id, e);
            return false;
        }
    }

    private void removeClient(ClientConnection client)
    {
        clients.remove(client);
        client.close();
        Logger.info(LOG_SOURCE, "client_disconnected", client.id);
    }

    private static void closeServerSocket(ServerSocket socket)
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

    private static class ClientConnection implements Closeable
    {
        private final String id;
        private final Socket socket;
        private final BufferedReader reader;
        private final BufferedWriter writer;
        private final Object writeLock = new Object();

        private ClientConnection(String id, Socket socket) throws IOException
        {
            this.id = id;
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public void close()
        {
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
}
