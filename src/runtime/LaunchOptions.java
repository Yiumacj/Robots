package runtime;

import log.Logger;

public class LaunchOptions
{
    private static final String LOG_SOURCE = "runtime.LaunchOptions";
    public static final int DEFAULT_PORT = 5050;
    public static final String DEFAULT_HOST = "localhost";

    private final LaunchMode mode;
    private final String host;
    private final int port;

    public LaunchOptions(LaunchMode mode, String host, int port)
    {
        this.mode = mode;
        this.host = host;
        this.port = port;
    }

    public LaunchMode getMode()
    {
        return mode;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public static LaunchOptions parse(String[] args)
    {
        LaunchMode mode = LaunchMode.LOCAL;
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args == null)
        {
            LaunchOptions options = new LaunchOptions(mode, host, port);
            Logger.info(LOG_SOURCE, "parse_args", "No CLI args provided, using defaults.");
            return options;
        }

        for (String arg : args)
        {
            if (arg == null || !arg.startsWith("--"))
            {
                continue;
            }
            int delimiter = arg.indexOf('=');
            String key = delimiter > 0 ? arg.substring(2, delimiter) : arg.substring(2);
            String value = delimiter > 0 ? arg.substring(delimiter + 1) : "";

            if ("mode".equals(key))
            {
                mode = parseMode(value);
            }
            else if ("host".equals(key) && !value.isEmpty())
            {
                host = value;
            }
            else if ("port".equals(key))
            {
                port = parsePort(value, port);
            }
        }

        LaunchOptions options = new LaunchOptions(mode, host, port);
        Logger.info(LOG_SOURCE, "parse_args", "mode=" + options.getMode() + ", host=" + options.getHost() + ", port=" + options.getPort());
        return options;
    }

    private static LaunchMode parseMode(String raw)
    {
        if ("dedicated".equalsIgnoreCase(raw))
        {
            return LaunchMode.DEDICATED;
        }
        if ("host".equalsIgnoreCase(raw))
        {
            return LaunchMode.HOST;
        }
        if ("client".equalsIgnoreCase(raw))
        {
            return LaunchMode.CLIENT;
        }
        return LaunchMode.LOCAL;
    }

    private static int parsePort(String raw, int fallback)
    {
        try
        {
            int parsed = Integer.parseInt(raw);
            if (parsed <= 0 || parsed > 65535)
            {
                return fallback;
            }
            return parsed;
        }
        catch (NumberFormatException ignored)
        {
            Logger.warn(LOG_SOURCE, "invalid_port", "Cannot parse port '" + raw + "', fallback=" + fallback);
            return fallback;
        }
    }
}
