package selftest;

public class SelfTestResult
{
    private final String name;
    private final boolean passed;
    private final long durationMillis;
    private final Throwable failure;

    public SelfTestResult(String name, boolean passed, long durationMillis, Throwable failure)
    {
        this.name = name;
        this.passed = passed;
        this.durationMillis = durationMillis;
        this.failure = failure;
    }

    public String getName()
    {
        return name;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public long getDurationMillis()
    {
        return durationMillis;
    }

    public Throwable getFailure()
    {
        return failure;
    }
}
