package log;

public class UiLogSink implements LogSink
{
    private final LogWindowSource source;

    public UiLogSink(LogWindowSource source)
    {
        this.source = source;
    }

    @Override
    public void append(LogEntry entry)
    {
        source.append(entry);
    }

    @Override
    public void close()
    {
        
    }
}
