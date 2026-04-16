package controller;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gui.RobotStateView;
import log.Logger;
import model.RobotState;
import network.RobotGameClient;
import network.RobotGameClientListener;
import network.protocol.ServerErrorEvent;
import network.protocol.ServerStateEvent;
import network.protocol.ServerWelcomeEvent;

public class RemoteGameController implements GameController, RobotGameClientListener
{
    private static final String LOG_SOURCE = "controller.RemoteGameController";
    private static final RobotState DEFAULT_STATE = new RobotState(100.0, 100.0, 0.0, 150, 100);

    private final RobotGameClient client;
    private final List<RobotStateView> views = new ArrayList<RobotStateView>();
    private final NetworkErrorHandler errorHandler;
    private volatile RobotState lastState = DEFAULT_STATE;

    public RemoteGameController(String host, int port, NetworkErrorHandler errorHandler)
    {
        this.client = new RobotGameClient(host, port, this);
        this.errorHandler = errorHandler;
    }

    @Override
    public synchronized void start()
    {
        try
        {
            client.connect();
            Logger.info(LOG_SOURCE, "connect", "Connected to remote game server.");
        }
        catch (IOException e)
        {
            String message = "Unable to connect to server: " + e.getMessage();
            Logger.error(LOG_SOURCE, "connect_failed", message, e);
            reportError(message);
        }
    }

    @Override
    public synchronized void stop()
    {
        Logger.info(LOG_SOURCE, "disconnect", "Closing remote client connection.");
        client.close();
    }

    @Override
    public synchronized void addView(RobotStateView view)
    {
        views.add(view);
        view.render(lastState);
    }

    @Override
    public void setTargetPosition(Point point)
    {
        Logger.debug(LOG_SOURCE, "set_target", "x=" + point.x + ", y=" + point.y);
        if (!client.sendSetTarget(point))
        {
            reportError("Cannot send command because server connection is not available.");
        }
    }

    @Override
    public void tick(double duration)
    {
        // Tick is controlled by remote server.
    }

    @Override
    public void onWelcome(ServerWelcomeEvent event)
    {
        if (event != null)
        {
            Logger.info(LOG_SOURCE, "welcome", "Connected as client " + event.getClientId());
        }
    }

    @Override
    public void onState(ServerStateEvent event)
    {
        if (event == null)
        {
            return;
        }
        lastState = event.toRobotState();
        publishState(lastState);
    }

    @Override
    public void onServerError(ServerErrorEvent event)
    {
        String message = event == null ? "Server reported an unknown error." : event.getMessage();
        Logger.error(LOG_SOURCE, "server_error", message);
        reportError(message);
    }

    @Override
    public void onDisconnected(String message)
    {
        Logger.warn(LOG_SOURCE, "disconnected", message);
        reportError(message);
    }

    private synchronized void publishState(RobotState state)
    {
        for (RobotStateView view : views)
        {
            view.render(state);
        }
    }

    private void reportError(String message)
    {
        if (errorHandler != null)
        {
            errorHandler.onError(message);
        }
    }

    public interface NetworkErrorHandler
    {
        void onError(String message);
    }
}
