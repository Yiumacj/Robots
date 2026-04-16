package selftest;

import org.junit.Assert;
import org.junit.Test;

public class SelfTestRunnerTest
{
    @Test
    public void discoversAndRunsRegisteredSelfTests()
    {
        SelfTestRunner runner = new SelfTestRunner();
        SelfTestReport report = runner.runAll();

        Assert.assertTrue(report.getTotalCount() >= 2);
        Assert.assertTrue(report.isSuccessful());
    }
}
