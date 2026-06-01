package service;

import java.text.MessageFormat;
import java.util.Locale;

public final class MessageFormattingBenchmark
{
    private static final int WARMUP_ITERATIONS = 20_000;
    private static final int MEASURE_ITERATIONS = 300_000;
    private static volatile int blackHole;

    private MessageFormattingBenchmark()
    {
    }

    public static void main(String[] args)
    {
        Locale locale = Locale.US;
        String formatterPattern = "Robot X: %.2f, Robot Y: %.2f, target: %d:%d";
        String messageFormatPattern = "Robot X: {0,number,0.00}, Robot Y: {1,number,0.00}, target: {2,number,0}:{3,number,0}";

        warmUp(locale, formatterPattern, messageFormatPattern);

        long formatterNanos = measureFormatter(locale, formatterPattern);
        long messageFormatNanos = measureMessageFormatWithoutCache(locale, messageFormatPattern);
        long cachedMessageFormatNanos = measureMessageFormatWithCache(locale, messageFormatPattern);

        System.out.println("Iterations: " + MEASURE_ITERATIONS);
        printResult("Formatter / String.format", formatterNanos);
        printResult("MessageFormat.format without cache", messageFormatNanos);
        printResult("MessageFormat with cached parsed pattern", cachedMessageFormatNanos);
        System.out.println("blackHole=" + blackHole);
    }

    private static void warmUp(Locale locale, String formatterPattern, String messageFormatPattern)
    {
        for (int i = 0; i < WARMUP_ITERATIONS; i++)
        {
            blackHole += String.format(locale, formatterPattern, 123.456, 654.321, 10, 20).length();
            blackHole += MessageFormat.format(messageFormatPattern, 123.456, 654.321, 10, 20).length();
            MessageFormat cachedFormat = new MessageFormat(messageFormatPattern, locale);
            blackHole += cachedFormat.format(new Object[] { 123.456, 654.321, 10, 20 }).length();
        }
    }

    private static long measureFormatter(Locale locale, String pattern)
    {
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++)
        {
            blackHole += String.format(locale, pattern, 123.456 + i, 654.321 + i, i, i + 1).length();
        }
        return System.nanoTime() - start;
    }

    private static long measureMessageFormatWithoutCache(Locale locale, String pattern)
    {
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++)
        {
            MessageFormat format = new MessageFormat(pattern, locale);
            blackHole += format.format(new Object[] { 123.456 + i, 654.321 + i, i, i + 1 }).length();
        }
        return System.nanoTime() - start;
    }

    private static long measureMessageFormatWithCache(Locale locale, String pattern)
    {
        MessageFormat cachedFormat = new MessageFormat(pattern, locale);
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++)
        {
            blackHole += cachedFormat.format(new Object[] { 123.456 + i, 654.321 + i, i, i + 1 }).length();
        }
        return System.nanoTime() - start;
    }

    private static void printResult(String label, long nanos)
    {
        double totalMs = nanos / 1_000_000.0;
        double nsPerOperation = (double) nanos / MEASURE_ITERATIONS;
        System.out.printf(Locale.US, "%s: %.2f ms, %.1f ns/op%n", label, totalMs, nsPerOperation);
    }
}
