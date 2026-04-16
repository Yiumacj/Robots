package network.protocol;

public class ServerErrorEvent
{
    private final String code;
    private final String message;

    public ServerErrorEvent(String code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public String getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }
}
