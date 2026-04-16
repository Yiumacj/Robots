package selftest.cases;

import runtime.LaunchMode;
import runtime.LaunchOptions;
import selftest.SelfTestCase;
import selftest.SelfTestContext;

public class LaunchOptionsSelfTest implements SelfTestCase
{
    @Override
    public String getName()
    {
        return "LaunchOptions parsing";
    }

    @Override
    public void run(SelfTestContext context)
    {
        LaunchOptions parsed = LaunchOptions.parse(new String[] {
                "--mode=client",
                "--host=10.10.10.10",
                "--port=6000"
        });
        context.assertEquals(LaunchMode.CLIENT, parsed.getMode(), "Mode should be CLIENT.");
        context.assertEquals("10.10.10.10", parsed.getHost(), "Host should match input.");
        context.assertEquals(Integer.valueOf(6000), Integer.valueOf(parsed.getPort()), "Port should match input.");
    }
}
