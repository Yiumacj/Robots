package plugin;

import java.io.File;
import java.util.List;

import controller.GameController;
import log.Logger;

public class RobotJarRunner implements Runnable
{
    private static final String LOG_SOURCE = "plugin.RobotJarRunner";

    private final File jarFile;
    private final String className;
    private final GameController controller;
    private final Runnable onFinish;
    private final RobotJarErrorHandler errorHandler;

    public RobotJarRunner(File jarFile, String className, GameController controller, Runnable onFinish, RobotJarErrorHandler errorHandler)
    {
        this.jarFile = jarFile;
        this.className = className;
        this.controller = controller;
        this.onFinish = onFinish;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run()
    {
        RobotJarLoader loader = null;
        try
        {
            if (controller == null)
            {
                throw new RobotJarException("Robot controller is not available.");
            }
            loader = new RobotJarLoader(jarFile);
            String resolvedClassName = resolveClassName(loader);
            RobotProgram program = loader.createProgram(jarFile, resolvedClassName);
            Thread.currentThread().setContextClassLoader(program.getClass().getClassLoader());
            Logger.info(LOG_SOURCE, "jar_started", "Running robot class " + resolvedClassName + " from " + jarFile.getAbsolutePath());
            program.run(new RobotContext(controller));
            Logger.info(LOG_SOURCE, "jar_finished", "Robot class finished: " + resolvedClassName);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            Logger.info(LOG_SOURCE, "jar_interrupted", "Robot class interrupted: " + jarFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            Logger.error(LOG_SOURCE, "jar_error", e.getMessage(), e);
            if (errorHandler != null)
            {
                errorHandler.onRobotJarError(e.getMessage());
            }
        }
        finally
        {
            if (loader != null)
            {
                try
                {
                    loader.close();
                }
                catch (Exception e)
                {
                    Logger.error(LOG_SOURCE, "jar_close_error", e.getMessage(), e);
                }
            }
            if (onFinish != null)
            {
                onFinish.run();
            }
        }
    }

    private String resolveClassName(RobotJarLoader loader) throws Exception
    {
        if (className != null && !className.trim().isEmpty())
        {
            return className;
        }
        List<String> classes = loader.findProgramClassNames(jarFile, true);
        if (classes.isEmpty())
        {
            throw new RobotJarException("No classes implementing plugin.RobotProgram were found in the jar.");
        }
        return classes.get(0);
    }

    public interface RobotJarErrorHandler
    {
        void onRobotJarError(String message);
    }
}
