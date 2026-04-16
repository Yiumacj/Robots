package log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Logger
{
    private static final LogWindowSource defaultLogSource;
    private static final List<LogSink> sinks = new CopyOnWriteArrayList<LogSink>();
    private static volatile boolean initialized;

    static {
        defaultLogSource = new LogWindowSource(100);
        initialize(new File(System.getProperty("user.dir"), "logs"));
    }

    private Logger()
    {
    }

    public static synchronized void initialize(File logsDir)
    {
        if (initialized)
        {
            return;
        }
        sinks.clear();
        sinks.addAll(Arrays.asList(
                new UiLogSink(defaultLogSource),
                new ConsoleLogSink(),
                new RollingFileLogSink(logsDir)));
        installUncaughtExceptionHandler();
        initialized = true;
    }

    static synchronized void replaceSinksForTesting(List<LogSink> testSinks)
    {
        initialized = true;
        sinks.clear();
        sinks.addAll(testSinks);
    }

    static synchronized void resetDefaultSinksForTesting()
    {
        initialized = false;
        initialize(new File(System.getProperty("user.dir"), "logs"));
    }

    public static synchronized void shutdown()
    {
        for (LogSink sink : sinks)
        {
            safeClose(sink);
        }
    }

    public static void trace(String message)
    {
        log(LogLevel.Trace, "app", "trace", message, null);
    }

    public static void debug(String strMessage)
    {
        log(LogLevel.Debug, "app", "debug", strMessage, null);
    }

    public static void info(String message)
    {
        log(LogLevel.Info, "app", "info", message, null);
    }

    public static void warn(String message)
    {
        log(LogLevel.Warning, "app", "warn", message, null);
    }

    public static void error(String message, Throwable throwable)
    {
        log(LogLevel.Error, "app", "error", message, throwable);
    }

    public static void error(String strMessage)
    {
        log(LogLevel.Error, "app", "error", strMessage, null);
    }

    public static void fatal(String message, Throwable throwable)
    {
        log(LogLevel.Fatal, "app", "fatal", message, throwable);
    }

    public static void trace(String source, String event, String details)
    {
        log(LogLevel.Trace, source, event, details, null);
    }

    public static void debug(String source, String event, String details)
    {
        log(LogLevel.Debug, source, event, details, null);
    }

    public static void info(String source, String event, String details)
    {
        log(LogLevel.Info, source, event, details, null);
    }

    public static void warn(String source, String event, String details)
    {
        log(LogLevel.Warning, source, event, details, null);
    }

    public static void error(String source, String event, String details)
    {
        log(LogLevel.Error, source, event, details, null);
    }

    public static void error(String source, String event, String details, Throwable throwable)
    {
        log(LogLevel.Error, source, event, details, throwable);
    }

    public static void fatal(String source, String event, String details, Throwable throwable)
    {
        log(LogLevel.Fatal, source, event, details, throwable);
    }

    private static void log(LogLevel level, String source, String event, String details, Throwable throwable)
    {
        LogEntry entry = new LogEntry(level, source, event, details, throwable);
        for (LogSink sink : sinks)
        {
            try
            {
                sink.append(entry);
            }
            catch (Exception e)
            {
                System.err.println("Log sink failure: " + e.getMessage());
            }
        }
    }

    public static LogWindowSource getDefaultLogSource()
    {
        return defaultLogSource;
    }

    private static void installUncaughtExceptionHandler()
    {
        Thread.UncaughtExceptionHandler handler = (thread, throwable) -> {
            String details = "Thread '" + thread.getName() + "' crashed.";
            fatal("runtime", "uncaught_exception", details, throwable);
            shutdown();
        };
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    static String stackTraceToString(Throwable throwable)
    {
        if (throwable == null)
        {
            return null;
        }
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }

    private static void safeClose(LogSink sink)
    {
        try
        {
            sink.close();
        }
        catch (Exception e)
        {
            System.err.println("Log sink close failure: " + e.getMessage());
        }
    }
}
