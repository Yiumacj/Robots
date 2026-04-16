package network.protocol;

public class ServerWelcomeEvent
{
    private final String clientId;

    public ServerWelcomeEvent(String clientId)
    {
        this.clientId = clientId;
    }

    public String getClientId()
    {
        return clientId;
    }
}
