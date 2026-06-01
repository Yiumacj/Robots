package network;

import network.protocol.ServerErrorEvent;
import network.protocol.ServerFullStateEvent;
import network.protocol.ServerStateEvent;
import network.protocol.ServerWelcomeEvent;

public interface RobotGameClientListener
{
    void onWelcome(ServerWelcomeEvent event);

    
    void onState(ServerStateEvent event);

    
    void onFullState(ServerFullStateEvent event);

    void onServerError(ServerErrorEvent event);

    void onDisconnected(String message);
}
