package log;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class LogWindowSourceTest
{
    @Test
    public void keepsOnlyConfiguredQueueLength()
    {
        LogWindowSource source = new LogWindowSource(3);
        source.append(LogLevel.Info, "one");
        source.append(LogLevel.Info, "two");
        source.append(LogLevel.Info, "three");
        source.append(LogLevel.Info, "four");

        Assert.assertEquals(3, source.size());
        Iterator<LogEntry> iterator = source.all().iterator();
        Assert.assertTrue(iterator.next().getMessage().contains("two"));
    }
}
