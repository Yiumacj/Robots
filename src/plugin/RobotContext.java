package plugin;

import java.awt.Color;
import java.awt.Point;

import controller.GameController;
import model.RobotState;

public class RobotContext
{
    private final GameController controller;
    private volatile RobotState lastState;

    public RobotContext(GameController controller)
    {
        this.controller = controller;
    }

    public void moveTo(int x, int y)
    {
        controller.setTargetPosition(new Point(x, y));
    }

    public void setTargetPosition(int x, int y)
    {
        moveTo(x, y);
    }

    public void setColor(int red, int green, int blue)
    {
        controller.setRobotColor(new Color(component(red), component(green), component(blue)));
    }

    public void waitFor(long milliseconds) throws InterruptedException
    {
        Thread.sleep(milliseconds);
    }

    public void sleep(long milliseconds) throws InterruptedException
    {
        waitFor(milliseconds);
    }

    public RobotState getLastState()
    {
        return lastState;
    }

    public void updateState(RobotState state)
    {
        lastState = state;
    }

    public void tick(double duration)
    {
        controller.tick(duration);
    }

    private static int component(int value)
    {
        if (value < 0 || value > 255)
        {
            throw new IllegalArgumentException("Color component must be in range 0..255.");
        }
        return value;
    }
}
