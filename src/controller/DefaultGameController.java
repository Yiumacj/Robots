package controller;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gui.RobotStateView;
import log.Logger;
import model.RobotModel;
import model.RobotState;

public class DefaultGameController implements GameController
{
    private static final String LOG_SOURCE = "controller.DefaultGameController";
    private final RobotModel model;
    private final List<RobotStateView> views = new ArrayList<RobotStateView>();
    private Timer timer;

    public DefaultGameController(RobotModel model)
    {
        this.model = model;
    }

    @Override
    public synchronized void start()
    {
        if (timer != null)
        {
            Logger.debug(LOG_SOURCE, "start_skipped", "Game timer is already running.");
            return;
        }
        Logger.info(LOG_SOURCE, "start", "Starting local game timer.");
        timer = new Timer("robot model timer", true);
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                tick(10);
            }
        }, 0, 10);
        publishState();
    }

    @Override
    public synchronized void stop()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
            Logger.info(LOG_SOURCE, "stop", "Local game timer stopped.");
        }
    }

    @Override
    public synchronized void addView(RobotStateView view)
    {
        views.add(view);
        Logger.debug(LOG_SOURCE, "add_view", "View count=" + views.size());
        view.render(model.getState());
    }

    @Override
    public void setTargetPosition(Point point)
    {
        Logger.debug(LOG_SOURCE, "set_target", "x=" + point.x + ", y=" + point.y);
        model.setTargetPosition(point);
        publishState();
    }

    @Override
    public void setRobotColor(Color color)
    {
        if (color == null) return;
        Logger.debug(LOG_SOURCE, "set_color", "rgb=" + color.getRGB());
        model.setRobotColorRgb(color.getRGB());
        publishState();
    }

    @Override
    public void tick(double duration)
    {
        model.update(duration);
        publishState();
    }

    private synchronized void publishState()
    {
        RobotState state = model.getState();
        for (RobotStateView view : views)
        {
            view.render(state);
        }
    }
}
