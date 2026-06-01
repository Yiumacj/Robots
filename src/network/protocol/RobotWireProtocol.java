package network.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public final class RobotWireProtocol
{
    public static final String TYPE_CLIENT_SET_TARGET  = "ClientCommand.SetTarget";
    public static final String TYPE_CLIENT_SET_COLOR   = "ClientCommand.SetColor";
    public static final String TYPE_SERVER_STATE       = "ServerEvent.State";
    public static final String TYPE_SERVER_FULL_STATE  = "ServerEvent.FullState";
    public static final String TYPE_SERVER_WELCOME     = "ServerEvent.Welcome";
    public static final String TYPE_SERVER_ERROR       = "ServerEvent.Error";

    private static final Gson GSON = new GsonBuilder().create();

    private RobotWireProtocol()
    {
    }

    public static String encodeMessage(String type, Object payload)
    {
        WireMessage message = new WireMessage(type, GSON.toJsonTree(payload));
        return GSON.toJson(message);
    }

    public static WireMessage decodeMessage(String json) throws JsonSyntaxException
    {
        return GSON.fromJson(json, WireMessage.class);
    }

    public static <T> T decodePayload(WireMessage message, Class<T> payloadType)
    {
        if (message == null || message.getPayload() == null)
        {
            return null;
        }
        return GSON.fromJson(message.getPayload(), payloadType);
    }
}
