package selftest;

public class SelfTestContext
{
    public void assertTrue(boolean condition, String message)
    {
        if (!condition)
        {
            throw new IllegalStateException(message);
        }
    }

    public void assertEquals(Object expected, Object actual, String message)
    {
        if (expected == null && actual == null)
        {
            return;
        }
        if (expected != null && expected.equals(actual))
        {
            return;
        }
        throw new IllegalStateException(message + " | expected=" + expected + ", actual=" + actual);
    }

    public void assertNotNull(Object value, String message)
    {
        if (value == null)
        {
            throw new IllegalStateException(message);
        }
    }
}
