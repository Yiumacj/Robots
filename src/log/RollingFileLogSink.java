package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RollingFileLogSink implements LogSink
{
    private static final String ACTIVE_LOG_NAME = "robots.log";
    private static final String ARCHIVE_PREFIX = "robots-";
    private static final String ARCHIVE_EXT = ".zip";
    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int DEFAULT_MAX_ARCHIVES = 10;

    private final File logsDir;
    private final long maxFileSizeBytes;
    private final int maxArchives;
    private final Object lock = new Object();

    private BufferedWriter writer;
    private File activeLogFile;

    public RollingFileLogSink(File logsDir)
    {
        this(logsDir, DEFAULT_MAX_FILE_SIZE_BYTES, DEFAULT_MAX_ARCHIVES);
    }

    public RollingFileLogSink(File logsDir, long maxFileSizeBytes, int maxArchives)
    {
        this.logsDir = logsDir;
        this.maxFileSizeBytes = maxFileSizeBytes <= 0 ? DEFAULT_MAX_FILE_SIZE_BYTES : maxFileSizeBytes;
        this.maxArchives = maxArchives <= 0 ? DEFAULT_MAX_ARCHIVES : maxArchives;
    }

    @Override
    public void append(LogEntry entry)
    {
        if (entry == null)
        {
            return;
        }
        synchronized (lock)
        {
            try
            {
                ensureWriter();
                rotateIfNeeded(entry.getMessage());
                writer.write(entry.getMessage());
                writer.newLine();
                writer.flush();
            }
            catch (IOException e)
            {
                System.err.println("File logging failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void close()
    {
        synchronized (lock)
        {
            closeWriterQuietly();
        }
    }

    private void ensureWriter() throws IOException
    {
        if (!logsDir.exists() && !logsDir.mkdirs())
        {
            throw new IOException("Cannot create logs directory: " + logsDir.getAbsolutePath());
        }
        if (activeLogFile == null)
        {
            activeLogFile = new File(logsDir, ACTIVE_LOG_NAME);
        }
        if (writer == null)
        {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(activeLogFile, true), StandardCharsets.UTF_8));
        }
    }

    private void rotateIfNeeded(String nextMessage) throws IOException
    {
        long estimatedIncomingBytes = nextMessage.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
        long currentSize = activeLogFile.exists() ? activeLogFile.length() : 0L;
        if (currentSize + estimatedIncomingBytes <= maxFileSizeBytes)
        {
            return;
        }

        closeWriterQuietly();
        if (!activeLogFile.exists() || activeLogFile.length() == 0L)
        {
            return;
        }

        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(new Date());
        File rotatedLog = new File(logsDir, ARCHIVE_PREFIX + stamp + ".log");
        Files.move(activeLogFile.toPath(), rotatedLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
        zipAndDelete(rotatedLog, new File(logsDir, ARCHIVE_PREFIX + stamp + ARCHIVE_EXT));
        cleanupOldArchives();
        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(activeLogFile, true), StandardCharsets.UTF_8));
    }

    private void zipAndDelete(File sourceLog, File zipFile) throws IOException
    {
        byte[] buffer = new byte[4096];
        try (ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipFile));
             FileInputStream input = new FileInputStream(sourceLog))
        {
            zipOutput.putNextEntry(new ZipEntry(sourceLog.getName()));
            int read;
            while ((read = input.read(buffer)) >= 0)
            {
                zipOutput.write(buffer, 0, read);
            }
            zipOutput.closeEntry();
        }
        if (!sourceLog.delete())
        {
            throw new IOException("Cannot delete rotated log: " + sourceLog.getAbsolutePath());
        }
    }

    private void cleanupOldArchives()
    {
        File[] archives = logsDir.listFiles((dir, name) -> name.startsWith(ARCHIVE_PREFIX) && name.endsWith(ARCHIVE_EXT));
        if (archives == null || archives.length <= maxArchives)
        {
            return;
        }
        List<File> sorted = new ArrayList<File>();
        for (File archive : archives)
        {
            sorted.add(archive);
        }
        sorted.sort(Comparator.comparingLong(File::lastModified).reversed());
        for (int i = maxArchives; i < sorted.size(); i++)
        {
            if (!sorted.get(i).delete())
            {
                System.err.println("Cannot delete old archive: " + sorted.get(i).getAbsolutePath());
            }
        }
    }

    private void closeWriterQuietly()
    {
        if (writer == null)
        {
            return;
        }
        try
        {
            writer.flush();
            writer.close();
        }
        catch (IOException ignored)
        {
            // Best effort during shutdown.
        }
        finally
        {
            writer = null;
        }
    }
}
