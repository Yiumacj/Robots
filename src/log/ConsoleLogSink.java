package log;

public class ConsoleLogSink implements LogSink
{
    @Override
    public void append(LogEntry entry)
    {
        if (entry == null)
        {
            return;
        }
        if (entry.getLevel().level() >= LogLevel.Error.level())
        {
            System.err.println(entry.getMessage());
            return;
        }
        System.out.println(entry.getMessage());
    }

    @Override
    public void close()
    {
        // Console stream lifecycle is managed by JVM.
    }
}
