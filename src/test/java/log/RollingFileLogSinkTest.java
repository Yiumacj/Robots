package log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

public class RollingFileLogSinkTest
{
    @Test
    public void rotatesArchivesAndKeepsRetentionLimit() throws Exception
    {
        File logsDir = Files.createTempDirectory("robots-log-test").toFile();
        RollingFileLogSink sink = new RollingFileLogSink(logsDir, 200, 2);
        try
        {
            for (int i = 0; i < 80; i++)
            {
                sink.append(new LogEntry(LogLevel.Info, "test", "rotate", "line-" + i + "-abcdefghijklmnopqrstuvwxyz", null));
            }
        }
        finally
        {
            sink.close();
        }

        File active = new File(logsDir, "robots.log");
        Assert.assertTrue(active.exists());
        Assert.assertTrue(active.length() > 0L);

        File[] archives = logsDir.listFiles((dir, name) -> name.endsWith(".zip"));
        Assert.assertNotNull(archives);
        Assert.assertTrue(archives.length > 0);
        Assert.assertTrue(archives.length <= 2);
        assertContainsLogEntry(archives[0]);
    }

    @Test
    public void flushesActiveLogOnClose() throws Exception
    {
        File logsDir = Files.createTempDirectory("robots-log-close").toFile();
        RollingFileLogSink sink = new RollingFileLogSink(logsDir, 1024 * 1024, 10);
        try
        {
            sink.append(new LogEntry(LogLevel.Info, "test", "flush", "value", null));
        }
        finally
        {
            sink.close();
        }

        File active = new File(logsDir, "robots.log");
        String content = readFile(active);
        Assert.assertTrue(content.contains("flush"));
        Assert.assertTrue(content.contains("value"));
    }

    private static void assertContainsLogEntry(File archiveFile) throws IOException
    {
        try (ZipFile zipFile = new ZipFile(archiveFile))
        {
            Assert.assertTrue(zipFile.entries().hasMoreElements());
            Assert.assertTrue(zipFile.entries().nextElement().getName().endsWith(".log"));
        }
    }

    private static String readFile(File file) throws IOException
    {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> copy = new ArrayList<String>(lines);
        return String.join("\n", copy);
    }
}
