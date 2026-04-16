package log;

import java.io.Closeable;

public interface LogSink extends Closeable
{
    void append(LogEntry entry);

    @Override
    void close();
}
