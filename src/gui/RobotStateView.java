package gui;

import model.MultiPlayerState;
import model.RobotState;

public interface RobotStateView
{
    
    void render(RobotState state);

    
    default void renderMulti(MultiPlayerState state)
    {
        render(state.getOwnState());
    }
}
