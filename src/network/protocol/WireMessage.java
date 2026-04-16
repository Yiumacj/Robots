package network.protocol;

import com.google.gson.JsonElement;

public class WireMessage
{
    private final String type;
    private final JsonElement payload;

    public WireMessage(String type, JsonElement payload)
    {
        this.type = type;
        this.payload = payload;
    }

    public String getType()
    {
        return type;
    }

    public JsonElement getPayload()
    {
        return payload;
    }
}
