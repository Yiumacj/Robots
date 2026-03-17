package controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gui.RobotStateView;
import model.RobotModel;
import model.RobotState;

public class DefaultGameController implements GameController
{
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
            return;
        }
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
        }
    }

    @Override
    public synchronized void addView(RobotStateView view)
    {
        views.add(view);
        view.render(model.getState());
    }

    @Override
    public void setTargetPosition(Point point)
    {
        model.setTargetPosition(point);
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
