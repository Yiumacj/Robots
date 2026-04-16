package runtime;

import org.junit.Assert;
import org.junit.Test;

public class LaunchOptionsTest
{
    @Test
    public void parseClientModeWithHostAndPort()
    {
        LaunchOptions options = LaunchOptions.parse(new String[] {
                "--mode=client",
                "--host=10.10.0.7",
                "--port=5501"
        });

        Assert.assertEquals(LaunchMode.CLIENT, options.getMode());
        Assert.assertEquals("10.10.0.7", options.getHost());
        Assert.assertEquals(5501, options.getPort());
    }

    @Test
    public void parseFallbacksForInvalidPortAndUnknownMode()
    {
        LaunchOptions options = LaunchOptions.parse(new String[] {
                "--mode=unknown",
                "--port=abc"
        });

        Assert.assertEquals(LaunchMode.LOCAL, options.getMode());
        Assert.assertEquals(LaunchOptions.DEFAULT_PORT, options.getPort());
    }
}
