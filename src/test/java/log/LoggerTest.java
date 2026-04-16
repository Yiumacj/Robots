package log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class LoggerTest
{
    @After
    public void tearDown()
    {
        Logger.shutdown();
        Logger.resetDefaultSinksForTesting();
    }

    @Test
    public void routesMessageToAllConfiguredSinks()
    {
        CapturingSink sinkA = new CapturingSink();
        CapturingSink sinkB = new CapturingSink();
        Logger.replaceSinksForTesting(Arrays.asList(sinkA, sinkB));

        Logger.info("test.source", "test_event", "payload");

        Assert.assertEquals(1, sinkA.entries.size());
        Assert.assertEquals(1, sinkB.entries.size());
    }

    @Test
    public void includesLevelSourceThreadAndThrowableInFormattedMessage()
    {
        CapturingSink sink = new CapturingSink();
        Logger.replaceSinksForTesting(Arrays.asList(sink));

        Logger.error("test.source", "boom", "Something failed", new IllegalStateException("failure"));

        Assert.assertEquals(1, sink.entries.size());
        String message = sink.entries.get(0).getMessage();
        Assert.assertTrue(message.contains("[Error]"));
        Assert.assertTrue(message.contains("[test.source]"));
        Assert.assertTrue(message.contains("boom"));
        Assert.assertTrue(message.contains("Something failed"));
        Assert.assertTrue(message.contains("IllegalStateException"));
    }

    private static class CapturingSink implements LogSink
    {
        private final List<LogEntry> entries = new ArrayList<LogEntry>();

        @Override
        public void append(LogEntry entry)
        {
            entries.add(entry);
        }

        @Override
        public void close()
        {
        }
    }
}
