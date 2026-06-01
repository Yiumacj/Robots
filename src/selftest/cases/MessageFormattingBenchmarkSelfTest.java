package selftest.cases;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import selftest.SelfTestCase;
import selftest.SelfTestContext;

public class MessageFormattingBenchmarkSelfTest implements SelfTestCase
{
    private static final int ITERATIONS = 200_000;
    private static final String FORMATTER_PATTERN = "Robot X: %.2f, Robot Y: %.2f, target: %d/%d";
    private static final String MESSAGE_FORMAT_PATTERN = "Robot X: {0,number,0.00}, Robot Y: {1,number,0.00}, target: {2,number,0}/{3,number,0}";

    @Override
    public String getName()
    {
        return "Message formatting benchmark";
    }

    @Override
    public void run(SelfTestContext context)
    {
        runBenchmark();
    }

    public static void main(String[] args)
    {
        runBenchmark();
    }

    private static void runBenchmark()
    {
        warmUp();
        long formatterNanos = measureFormatter();
        long messageFormatNanos = measureMessageFormatWithoutCache();
        long cachedMessageFormatNanos = measureMessageFormatWithCache();

        System.out.println("Iterations: " + ITERATIONS);
        printResult("Formatter", formatterNanos);
        printResult("MessageFormat without cache", messageFormatNanos);
        printResult("MessageFormat with cache", cachedMessageFormatNanos);
        System.out.println("Cached MessageFormat speedup vs no cache: " +
                String.format(Locale.US, "%.2fx", (double) messageFormatNanos / cachedMessageFormatNanos));
        System.out.println("Cached MessageFormat speedup vs Formatter: " +
                String.format(Locale.US, "%.2fx", (double) formatterNanos / cachedMessageFormatNanos));
    }

    private static void warmUp()
    {
        for (int i = 0; i < 3; i++)
        {
            measureFormatter();
            measureMessageFormatWithoutCache();
            measureMessageFormatWithCache();
        }
    }

    private static long measureFormatter()
    {
        long start = System.nanoTime();
        int checksum = 0;
        for (int i = 0; i < ITERATIONS; i++)
        {
            String result = String.format(Locale.US, FORMATTER_PATTERN, 100.0 + i, 200.0 + i, i % 800, i % 600);
            checksum += result.length();
        }
        return elapsed(start, checksum);
    }

    private static long measureMessageFormatWithoutCache()
    {
        long start = System.nanoTime();
        int checksum = 0;
        for (int i = 0; i < ITERATIONS; i++)
        {
            String result = MessageFormat.format(MESSAGE_FORMAT_PATTERN, 100.0 + i, 200.0 + i, i % 800, i % 600);
            checksum += result.length();
        }
        return elapsed(start, checksum);
    }

    private static long measureMessageFormatWithCache()
    {
        Map<String, MessageFormat> cache = new HashMap<String, MessageFormat>();
        MessageFormat format = cache.computeIfAbsent(MESSAGE_FORMAT_PATTERN,
                key -> new MessageFormat(key, Locale.US));

        long start = System.nanoTime();
        int checksum = 0;
        for (int i = 0; i < ITERATIONS; i++)
        {
            String result;
            synchronized (format)
            {
                result = format.format(new Object[] {100.0 + i, 200.0 + i, i % 800, i % 600});
            }
            checksum += result.length();
        }
        return elapsed(start, checksum);
    }

    private static long elapsed(long start, int checksum)
    {
        if (checksum == 0)
        {
            throw new IllegalStateException("Benchmark was optimized away");
        }
        return System.nanoTime() - start;
    }

    private static void printResult(String title, long nanos)
    {
        double millis = nanos / 1_000_000.0;
        double nanosPerOperation = (double) nanos / ITERATIONS;
        System.out.println(title + ": " + String.format(Locale.US, "%.3f ms, %.1f ns/op", millis, nanosPerOperation));
    }
}
