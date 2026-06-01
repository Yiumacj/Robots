package plugin;

import java.io.IOException;

public class RobotJarException extends IOException
{
    public RobotJarException(String message)
    {
        super(message);
    }

    public RobotJarException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
