package model;

import java.awt.Color;

public class RobotState
{
    public static final int DEFAULT_COLOR_RGB = Color.MAGENTA.getRGB();

    private final double robotPositionX;
    private final double robotPositionY;
    private final double robotDirection;
    private final int targetPositionX;
    private final int targetPositionY;
    private final int robotColorRgb;

    public RobotState(double robotPositionX, double robotPositionY, double robotDirection,
                      int targetPositionX, int targetPositionY)
    {
        this(robotPositionX, robotPositionY, robotDirection, targetPositionX, targetPositionY, DEFAULT_COLOR_RGB);
    }

    public RobotState(double robotPositionX, double robotPositionY, double robotDirection,
                      int targetPositionX, int targetPositionY, int robotColorRgb)
    {
        this.robotPositionX = robotPositionX;
        this.robotPositionY = robotPositionY;
        this.robotDirection = robotDirection;
        this.targetPositionX = targetPositionX;
        this.targetPositionY = targetPositionY;
        this.robotColorRgb = robotColorRgb;
    }

    public double getRobotPositionX()
    {
        return robotPositionX;
    }

    public double getRobotPositionY()
    {
        return robotPositionY;
    }

    public double getRobotDirection()
    {
        return robotDirection;
    }

    public int getTargetPositionX()
    {
        return targetPositionX;
    }

    public int getTargetPositionY()
    {
        return targetPositionY;
    }

    public int getRobotColorRgb()
    {
        return robotColorRgb;
    }

    public Color getRobotColor()
    {
        return new Color(robotColorRgb, true);
    }
}
