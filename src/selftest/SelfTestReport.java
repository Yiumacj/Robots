package selftest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelfTestReport
{
    private final List<SelfTestResult> results;

    public SelfTestReport(List<SelfTestResult> results)
    {
        this.results = new ArrayList<SelfTestResult>(results);
    }

    public List<SelfTestResult> getResults()
    {
        return Collections.unmodifiableList(results);
    }

    public boolean isSuccessful()
    {
        for (SelfTestResult result : results)
        {
            if (!result.isPassed())
            {
                return false;
            }
        }
        return !results.isEmpty();
    }

    public int getTotalCount()
    {
        return results.size();
    }

    public int getFailedCount()
    {
        int failed = 0;
        for (SelfTestResult result : results)
        {
            if (!result.isPassed())
            {
                failed++;
            }
        }
        return failed;
    }
}
