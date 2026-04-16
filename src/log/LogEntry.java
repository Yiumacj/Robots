package log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogEntry
{
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private final Instant timestamp;
    private final LogLevel level;
    private final String threadName;
    private final String source;
    private final String event;
    private final String details;
    private final String throwable;
    private final String formattedMessage;

    public LogEntry(LogLevel level, String message)
    {
        this(level, "app", "message", message, null);
    }

    public LogEntry(LogLevel level, String source, String event, String details, Throwable throwable)
    {
        this.timestamp = Instant.now();
        this.level = level == null ? LogLevel.Info : level;
        this.threadName = Thread.currentThread().getName();
        this.source = source == null || source.trim().isEmpty() ? "app" : source;
        this.event = event == null || event.trim().isEmpty() ? "event" : event;
        this.details = details == null ? "" : details;
        this.throwable = Logger.stackTraceToString(throwable);
        this.formattedMessage = formatMessage();
    }

    public String getMessage()
    {
        return formattedMessage;
    }

    public LogLevel getLevel()
    {
        return level;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public String getSource()
    {
        return source;
    }

    public String getEvent()
    {
        return event;
    }

    public String getDetails()
    {
        return details;
    }

    public String getThrowable()
    {
        return throwable;
    }

    private String formatMessage()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(TIMESTAMP_FORMAT.format(timestamp)).append(']')
                .append('[').append(level.name()).append(']')
                .append('[').append(threadName).append(']')
                .append('[').append(source).append("] ")
                .append(event);
        if (!details.isEmpty())
        {
            builder.append(" | ").append(details);
        }
        if (throwable != null && !throwable.isEmpty())
        {
            builder.append(System.lineSeparator()).append(throwable);
        }
        return builder.toString();
    }
}

