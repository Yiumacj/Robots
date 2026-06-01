package network.protocol;

import java.util.List;





public class ServerFullStateEvent
{
    private final List<ServerStateEvent> players;
    private final long serverTick;

    public ServerFullStateEvent(List<ServerStateEvent> players, long serverTick)
    {
        this.players = players;
        this.serverTick = serverTick;
    }

    public List<ServerStateEvent> getPlayers()
    {
        return players;
    }

    public long getServerTick()
    {
        return serverTick;
    }
}
