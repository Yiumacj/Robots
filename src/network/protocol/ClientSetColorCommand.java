package network.protocol;

public class ClientSetColorCommand
{
    private final int rgb;

    public ClientSetColorCommand(int rgb)
    {
        this.rgb = rgb;
    }

    public int getRgb()
    {
        return rgb;
    }
}
