package model;

public class RobotState
{
    private final double robotPositionX;
    private final double robotPositionY;
    private final double robotDirection;
    private final int targetPositionX;
    private final int targetPositionY;

    public RobotState(double robotPositionX, double robotPositionY, double robotDirection,
                      int targetPositionX, int targetPositionY)
    {
        this.robotPositionX = robotPositionX;
        this.robotPositionY = robotPositionY;
        this.robotDirection = robotDirection;
        this.targetPositionX = targetPositionX;
        this.targetPositionY = targetPositionY;
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
}
