package script;

public class RobotScriptException extends Exception
{
    public RobotScriptException(String message)
    {
        super(message);
    }

    public RobotScriptException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
