package selftest;

public interface SelfTestCase
{
    String getName();

    void run(SelfTestContext context) throws Exception;
}
