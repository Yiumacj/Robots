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
    private final String playerId;
    private final int robotColorRgb;

    public ServerStateEvent(double robotX, double robotY, double direction,
                            int targetX, int targetY, long serverTick, String playerId)
    {
        this(robotX, robotY, direction, targetX, targetY, serverTick, playerId, RobotState.DEFAULT_COLOR_RGB);
    }

    public ServerStateEvent(double robotX, double robotY, double direction,
                            int targetX, int targetY, long serverTick, String playerId, int robotColorRgb)
    {
        this.robotX = robotX;
        this.robotY = robotY;
        this.direction = direction;
        this.targetX = targetX;
        this.targetY = targetY;
        this.serverTick = serverTick;
        this.playerId = playerId;
        this.robotColorRgb = robotColorRgb;
    }

    public static ServerStateEvent fromState(RobotState state, long serverTick, String playerId)
    {
        return new ServerStateEvent(
                state.getRobotPositionX(),
                state.getRobotPositionY(),
                state.getRobotDirection(),
                state.getTargetPositionX(),
                state.getTargetPositionY(),
                serverTick,
                playerId,
                state.getRobotColorRgb());
    }

    public RobotState toRobotState()
    {
        return new RobotState(robotX, robotY, direction, targetX, targetY, robotColorRgb);
    }

    public long getServerTick()
    {
        return serverTick;
    }

    public String getPlayerId()
    {
        return playerId;
    }

    public int getRobotColorRgb()
    {
        return robotColorRgb;
    }
}
