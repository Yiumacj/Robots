package network.protocol;

import model.RobotState;

public class ServerStateEvent
{
    private final double robotX;
    private final double robotY;
    private final double direction;
    private final int targetX;
    private final int targetY;
    private final long serverTick;

    public ServerStateEvent(double robotX, double robotY, double direction, int targetX, int targetY, long serverTick)
    {
        this.robotX = robotX;
        this.robotY = robotY;
        this.direction = direction;
        this.targetX = targetX;
        this.targetY = targetY;
        this.serverTick = serverTick;
    }

    public static ServerStateEvent fromState(RobotState state, long serverTick)
    {
        return new ServerStateEvent(
                state.getRobotPositionX(),
                state.getRobotPositionY(),
                state.getRobotDirection(),
                state.getTargetPositionX(),
                state.getTargetPositionY(),
                serverTick);
    }

    public RobotState toRobotState()
    {
        return new RobotState(robotX, robotY, direction, targetX, targetY);
    }

    public long getServerTick()
    {
        return serverTick;
    }
}
