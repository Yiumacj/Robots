package selftest;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import log.Logger;

public class SelfTestRunner
{
    private static final String LOG_SOURCE = "selftest.SelfTestRunner";

    public SelfTestReport runAll()
    {
        List<SelfTestCase> tests = loadTests();
        Logger.info(LOG_SOURCE, "discovery", "Discovered " + tests.size() + " self-tests.");

        List<SelfTestResult> results = new ArrayList<SelfTestResult>();
        SelfTestContext context = new SelfTestContext();
        for (SelfTestCase test : tests)
        {
            long startedAt = System.currentTimeMillis();
            try
            {
                test.run(context);
                long duration = System.currentTimeMillis() - startedAt;
                results.add(new SelfTestResult(test.getName(), true, duration, null));
                Logger.info(LOG_SOURCE, "test_passed", test.getName() + " (" + duration + " ms)");
            }
            catch (Throwable failure)
            {
                long duration = System.currentTimeMillis() - startedAt;
                results.add(new SelfTestResult(test.getName(), false, duration, failure));
                Logger.error(LOG_SOURCE, "test_failed", test.getName() + " (" + duration + " ms)", failure);
            }
        }

        SelfTestReport report = new SelfTestReport(results);
        Logger.info(LOG_SOURCE, "summary", "total=" + report.getTotalCount() + ", failed=" + report.getFailedCount());
        return report;
    }

    private List<SelfTestCase> loadTests()
    {
        List<SelfTestCase> tests = new ArrayList<SelfTestCase>();
        ServiceLoader<SelfTestCase> loader = ServiceLoader.load(SelfTestCase.class);
        for (SelfTestCase test : loader)
        {
            tests.add(test);
        }
        return tests;
    }
}
