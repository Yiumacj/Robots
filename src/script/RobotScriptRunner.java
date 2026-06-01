package script;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.GameController;
import log.Logger;

public class RobotScriptRunner implements Runnable
{
    private static final String LOG_SOURCE = "script.RobotScriptRunner";

    private static final Pattern MOVE_PATTERN = Pattern.compile("^(?:moveTo|move_to|target)\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*;?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern WAIT_PATTERN = Pattern.compile("^(?:wait|sleep)\\s*\\(\\s*(\\d+)\\s*\\)\\s*;?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR_PATTERN = Pattern.compile("^(?:color|setColor|set_color)\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*;?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FOR_PATTERN = Pattern.compile("^for\\s+[A-Za-z_][A-Za-z0-9_]*\\s*=\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*do\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern END_PATTERN = Pattern.compile("^end\\s*;?$", Pattern.CASE_INSENSITIVE);

    private final File scriptFile;
    private final GameController controller;
    private final Runnable onFinish;
    private final ScriptErrorHandler errorHandler;

    public RobotScriptRunner(File scriptFile, GameController controller, Runnable onFinish, ScriptErrorHandler errorHandler)
    {
        this.scriptFile = scriptFile;
        this.controller = controller;
        this.onFinish = onFinish;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run()
    {
        try
        {
            if (controller == null)
            {
                throw new RobotScriptException("Robot controller is not available.");
            }
            List<String> lines = loadLines(scriptFile);
            Logger.info(LOG_SOURCE, "script_started", "Running script: " + scriptFile.getAbsolutePath());
            executeBlock(lines, 0, lines.size());
            Logger.info(LOG_SOURCE, "script_finished", "Script finished: " + scriptFile.getAbsolutePath());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            Logger.info(LOG_SOURCE, "script_interrupted", "Script interrupted: " + scriptFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            Logger.error(LOG_SOURCE, "script_error", e.getMessage(), e);
            if (errorHandler != null)
            {
                errorHandler.onScriptError(e.getMessage());
            }
        }
        finally
        {
            if (onFinish != null)
            {
                onFinish.run();
            }
        }
    }

    private static List<String> loadLines(File file) throws IOException
    {
        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> result = new ArrayList<String>();
        for (String rawLine : rawLines)
        {
            String line = removeComment(rawLine).trim();
            if (!line.isEmpty())
            {
                result.add(line);
            }
        }
        return result;
    }

    private static String removeComment(String line)
    {
        int commentIndex = line.indexOf("--");
        return commentIndex >= 0 ? line.substring(0, commentIndex) : line;
    }

    private int executeBlock(List<String> lines, int startInclusive, int endExclusive) throws RobotScriptException, InterruptedException
    {
        int index = startInclusive;
        while (index < endExclusive)
        {
            ensureNotInterrupted();
            String line = lines.get(index);

            Matcher forMatcher = FOR_PATTERN.matcher(line);
            if (forMatcher.matches())
            {
                int blockEnd = findMatchingEnd(lines, index + 1, endExclusive);
                int from = Integer.parseInt(forMatcher.group(1));
                int to = Integer.parseInt(forMatcher.group(2));
                int step = from <= to ? 1 : -1;
                for (int i = from; i != to + step; i += step)
                {
                    ensureNotInterrupted();
                    executeBlock(lines, index + 1, blockEnd);
                }
                index = blockEnd + 1;
                continue;
            }

            if (END_PATTERN.matcher(line).matches())
            {
                return index;
            }

            executeCommand(line, index + 1);
            index++;
        }
        return index;
    }

    private static int findMatchingEnd(List<String> lines, int startInclusive, int endExclusive) throws RobotScriptException
    {
        int nested = 0;
        for (int i = startInclusive; i < endExclusive; i++)
        {
            String line = lines.get(i);
            if (FOR_PATTERN.matcher(line).matches())
            {
                nested++;
            }
            else if (END_PATTERN.matcher(line).matches())
            {
                if (nested == 0)
                {
                    return i;
                }
                nested--;
            }
        }
        throw new RobotScriptException("Missing 'end' for for-loop.");
    }

    private void executeCommand(String line, int sourceLineNumber) throws RobotScriptException, InterruptedException
    {
        Matcher moveMatcher = MOVE_PATTERN.matcher(line);
        if (moveMatcher.matches())
        {
            int x = Integer.parseInt(moveMatcher.group(1));
            int y = Integer.parseInt(moveMatcher.group(2));
            controller.setTargetPosition(new Point(x, y));
            return;
        }

        Matcher waitMatcher = WAIT_PATTERN.matcher(line);
        if (waitMatcher.matches())
        {
            long ms = Long.parseLong(waitMatcher.group(1));
            Thread.sleep(ms);
            return;
        }

        Matcher colorMatcher = COLOR_PATTERN.matcher(line);
        if (colorMatcher.matches())
        {
            int r = parseColorComponent(colorMatcher.group(1), sourceLineNumber);
            int g = parseColorComponent(colorMatcher.group(2), sourceLineNumber);
            int b = parseColorComponent(colorMatcher.group(3), sourceLineNumber);
            controller.setRobotColor(new Color(r, g, b));
            return;
        }

        throw new RobotScriptException("Unsupported script command at line " + sourceLineNumber + ": " + line);
    }

    private static int parseColorComponent(String text, int sourceLineNumber) throws RobotScriptException
    {
        int value = Integer.parseInt(text);
        if (value < 0 || value > 255)
        {
            throw new RobotScriptException("Color component must be in range 0..255 at line " + sourceLineNumber + ".");
        }
        return value;
    }

    private static void ensureNotInterrupted() throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }
    }

    public interface ScriptErrorHandler
    {
        void onScriptError(String message);
    }
}
