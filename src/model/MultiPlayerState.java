package model;

import java.util.List;




public class MultiPlayerState
{
    private final RobotState ownState;
    private final List<RobotState> opponentStates;

    public MultiPlayerState(RobotState ownState, List<RobotState> opponentStates)
    {
        this.ownState = ownState;
        this.opponentStates = opponentStates;
    }

    public RobotState getOwnState()
    {
        return ownState;
    }

    public List<RobotState> getOpponentStates()
    {
        return opponentStates;
    }
}
