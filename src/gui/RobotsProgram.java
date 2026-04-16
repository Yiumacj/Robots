package gui;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.ApplicationController;
import controller.DefaultApplicationController;
import controller.DefaultGameController;
import controller.GameController;
import controller.RemoteGameController;
import log.Logger;
import model.AppLocale;
import model.RobotModel;
import network.RobotGameServer;
import runtime.LaunchMode;
import runtime.LaunchOptions;
import selftest.SelfTestReport;
import selftest.SelfTestResult;
import selftest.SelfTestRunner;
import service.ApplicationSettingsService;
import service.FileWindowStateService;
import service.LocalizationService;
import service.ResourceBundleLocalizationService;

public class RobotsProgram
{
    private static final String LOG_SOURCE = "gui.RobotsProgram";

    public static void main(String[] args)
    {
        Logger.initialize(new File(System.getProperty("user.dir"), "logs"));
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::shutdown, "logger-shutdown"));

        LaunchOptions options = LaunchOptions.parse(args);
        Logger.info(LOG_SOURCE, "launch", "mode=" + options.getMode() + ", host=" + options.getHost() + ", port=" + options.getPort());
        if (!runSelfTestsOrStop(options))
        {
            Logger.fatal(LOG_SOURCE, "startup_aborted", "Startup aborted because self-tests failed.", null);
            Logger.shutdown();
            return;
        }
        if (options.getMode() == LaunchMode.DEDICATED)
        {
            runDedicatedServer(options);
            return;
        }

        try
        {
            configureRussianLocale();
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e)
        {
            Logger.warn(LOG_SOURCE, "look_and_feel_init_failed", e.getMessage());
        }

        RobotGameServer embeddedServer = null;
        if (options.getMode() == LaunchMode.HOST)
        {
            try
            {
                embeddedServer = new RobotGameServer(options.getPort());
                embeddedServer.start();
            }
            catch (Exception e)
            {
                Logger.error(LOG_SOURCE, "host_server_start_failed", "Unable to start host server.", e);
                if (embeddedServer != null)
                {
                    embeddedServer.stop();
                }
                return;
            }
        }

        RobotGameServer serverForShutdown = embeddedServer;
        if (serverForShutdown != null)
        {
            Runtime.getRuntime().addShutdownHook(new Thread(serverForShutdown::stop, "host-server-shutdown"));
            Logger.debug(LOG_SOURCE, "host_shutdown_hook", "Host shutdown hook registered.");
        }

        RobotGameServer finalEmbeddedServer = embeddedServer;
        SwingUtilities.invokeLater(() -> startUi(options, finalEmbeddedServer));
    }

    private static void configureRussianLocale()
    {
        Locale.setDefault(new Locale("ru", "RU"));
    }

    private static void configureOptionPaneTexts(LocalizationService localizationService)
    {
        UIManager.put("OptionPane.yesButtonText", localizationService.get("option.yes"));
        UIManager.put("OptionPane.noButtonText", localizationService.get("option.no"));
        UIManager.put("OptionPane.cancelButtonText", localizationService.get("option.cancel"));
        UIManager.put("OptionPane.okButtonText", localizationService.get("option.ok"));
    }

    private static void startUi(LaunchOptions options, RobotGameServer embeddedServer)
    {
        ApplicationSettingsService settingsService = new FileWindowStateService();
        LocalizationService localizationService = new ResourceBundleLocalizationService();
        AppLocale locale = settingsService.loadLocale();
        localizationService.setCurrentLocale(locale);
        configureOptionPaneTexts(localizationService);

        MainApplicationFrame frame = new MainApplicationFrame(localizationService);
        GameController gameController = createGameController(options, frame, localizationService);

        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource(), localizationService);
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);

        GameWindow gameWindow = new GameWindow(localizationService);
        gameWindow.setSize(400, 400);
        gameWindow.setLocation(320, 10);
        gameWindow.setController(gameController);
        gameController.addView(gameWindow);

        RobotCoordinatesWindow coordinatesWindow = new RobotCoordinatesWindow(localizationService);
        coordinatesWindow.setLocation(730, 10);
        gameController.addView(coordinatesWindow);

        SettingsWindow settingsWindow = new SettingsWindow(localizationService);
        settingsWindow.setLocation(420, 180);

        ApplicationController controller = new DefaultApplicationController(
                frame,
                gameController,
                logWindow,
                gameWindow,
                coordinatesWindow,
                settingsWindow,
                localizationService,
                settingsService);
        frame.setController(controller);
        settingsWindow.setController(controller);
        frame.setVisible(true);
        controller.start();
        if (embeddedServer != null)
        {
            Logger.info(LOG_SOURCE, "host_ready", "Server and local client running on port " + embeddedServer.getPort());
        }
    }

    private static GameController createGameController(
            LaunchOptions options,
            MainApplicationFrame frame,
            LocalizationService localizationService)
    {
        if (options.getMode() == LaunchMode.CLIENT || options.getMode() == LaunchMode.HOST)
        {
            String host = options.getMode() == LaunchMode.HOST ? "localhost" : options.getHost();
            return new RemoteGameController(host, options.getPort(), message -> SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            frame,
                            message,
                            localizationService.get("dialog.network_error.title"),
                            JOptionPane.ERROR_MESSAGE)));
        }

        RobotModel robotModel = new RobotModel();
        Logger.info(LOG_SOURCE, "local_mode_controller", "Using local game controller.");
        return new DefaultGameController(robotModel);
    }

    private static void runDedicatedServer(LaunchOptions options)
    {
        final RobotGameServer server = new RobotGameServer(options.getPort());
        try
        {
            server.start();
            Logger.info(LOG_SOURCE, "dedicated_ready", "Dedicated server started on port " + server.getPort());
        }
        catch (Exception e)
        {
            Logger.error(LOG_SOURCE, "dedicated_start_failed", "Failed to start dedicated server.", e);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "dedicated-server-shutdown"));

        CountDownLatch blocker = new CountDownLatch(1);
        try
        {
            blocker.await();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            Logger.warn(LOG_SOURCE, "dedicated_interrupted", "Dedicated wait interrupted, stopping server.");
            server.stop();
        }
    }

    private static boolean runSelfTestsOrStop(LaunchOptions options)
    {
        SelfTestRunner runner = new SelfTestRunner();
        SelfTestReport report = runner.runAll();
        if (report.isSuccessful())
        {
            Logger.info(LOG_SOURCE, "self_tests_ok", "All self-tests passed before startup.");
            return true;
        }

        String failureSummary = buildFailureSummary(report);
        Logger.fatal(LOG_SOURCE, "self_tests_failed", failureSummary, null);

        if (options.getMode() != LaunchMode.DEDICATED)
        {
            JOptionPane.showMessageDialog(
                    null,
                    "Self-tests failed. Application startup is blocked.\n\n" + failureSummary,
                    "Startup blocked",
                    JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            System.err.println("Self-tests failed. Startup blocked.");
            System.err.println(failureSummary);
        }
        return false;
    }

    private static String buildFailureSummary(SelfTestReport report)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("failed=").append(report.getFailedCount())
                .append(", total=").append(report.getTotalCount());

        for (SelfTestResult result : report.getResults())
        {
            if (!result.isPassed())
            {
                builder.append("\n- ").append(result.getName());
                Throwable failure = result.getFailure();
                if (failure != null && failure.getMessage() != null)
                {
                    builder.append(": ").append(failure.getMessage());
                }
            }
        }
        return builder.toString();
    }
}
