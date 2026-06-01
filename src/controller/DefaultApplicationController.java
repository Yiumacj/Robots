package controller;

import java.util.Locale;

import javax.swing.UIManager;

import gui.GameWindow;
import gui.LocalizableView;
import gui.LogWindow;
import gui.MainApplicationFrame;
import gui.RobotCoordinatesWindow;
import gui.SettingsWindow;
import log.Logger;
import model.AppLocale;
import model.AppWindowKey;
import service.ApplicationSettingsService;
import service.LocalizationService;

public class DefaultApplicationController implements ApplicationController
{
    private static final String LOG_SOURCE = "controller.DefaultApplicationController";

    private final MainApplicationFrame frame;
    private final GameController gameController;
    private final GameController secondGameController;
    private final LogWindow logWindow;
    private final GameWindow gameWindow;
    private final GameWindow secondGameWindow;
    private final RobotCoordinatesWindow coordinatesWindow;
    private final SettingsWindow settingsWindow;
    private final LocalizationService localizationService;
    private final ApplicationSettingsService settingsService;
    private final LocalizableView[] localizableViews;
    private boolean secondGameControllerStarted;

    public DefaultApplicationController(MainApplicationFrame frame,
                                        GameController gameController,
                                        LogWindow logWindow,
                                        GameWindow gameWindow,
                                        RobotCoordinatesWindow coordinatesWindow,
                                        SettingsWindow settingsWindow,
                                        LocalizationService localizationService,
                                        ApplicationSettingsService settingsService)
    {
        this(frame, gameController, gameController, logWindow, gameWindow,
                new GameWindow(localizationService), coordinatesWindow, settingsWindow,
                localizationService, settingsService);
    }

    public DefaultApplicationController(MainApplicationFrame frame,
                                        GameController gameController,
                                        GameController secondGameController,
                                        LogWindow logWindow,
                                        GameWindow gameWindow,
                                        GameWindow secondGameWindow,
                                        RobotCoordinatesWindow coordinatesWindow,
                                        SettingsWindow settingsWindow,
                                        LocalizationService localizationService,
                                        ApplicationSettingsService settingsService)
    {
        this.frame = frame;
        this.gameController = gameController;
        this.secondGameController = secondGameController;
        this.logWindow = logWindow;
        this.gameWindow = gameWindow;
        this.secondGameWindow = secondGameWindow;
        this.coordinatesWindow = coordinatesWindow;
        this.settingsWindow = settingsWindow;
        this.localizationService = localizationService;
        this.settingsService = settingsService;
        this.localizableViews = new LocalizableView[] { frame, logWindow, gameWindow, secondGameWindow, coordinatesWindow, settingsWindow };
    }

    @Override
    public void start()
    {
        Logger.info(LOG_SOURCE, "start", "Initializing application windows and game loop.");
        frame.addWindow(AppWindowKey.LOG, logWindow);
        frame.addWindow(AppWindowKey.GAME, gameWindow);
        frame.addWindow(AppWindowKey.GAME_SECOND, secondGameWindow);
        secondGameWindow.setVisible(false);
        frame.addWindow(AppWindowKey.COORDINATES, coordinatesWindow);
        frame.addWindow(AppWindowKey.SETTINGS, settingsWindow);
        settingsWindow.setVisible(false);
        frame.setMinimumSize(logWindow.getSize());
        settingsWindow.setSelectedLocale(localizationService.getCurrentLocale());
        settingsService.loadWindowState(frame);
        refreshLocalizedUi();
        Logger.debug(LOG_SOURCE, "start", localizationService.get("log.protocol_started"));
        gameController.start();
    }

    @Override
    public void exit()
    {
        Logger.info(LOG_SOURCE, "exit_requested", "User requested application shutdown.");
        if (!frame.confirmExit())
        {
            Logger.debug(LOG_SOURCE, "exit_cancelled", "Exit dialog declined by user.");
            return;
        }
        gameController.stop();
        secondGameController.stop();
        settingsService.saveWindowState(frame);
        Logger.info(LOG_SOURCE, "exit", "Window state saved. Closing application.");
        frame.closeApplication();
    }

    @Override
    public void setLookAndFeel(String className)
    {
        Logger.info(LOG_SOURCE, "set_look_and_feel", className);
        frame.applyLookAndFeel(className);
        frame.invalidate();
    }

    @Override
    public void showWindow(AppWindowKey key)
    {
        Logger.debug(LOG_SOURCE, "show_window", String.valueOf(key));
        if (key == AppWindowKey.GAME_SECOND)
        {
            openSecondRobotWindow();
            return;
        }
        frame.activateWindow(key);
    }

    @Override
    public void openSecondRobotWindow()
    {
        Logger.debug(LOG_SOURCE, "open_second_robot_window", "Opening second robot window.");
        if (!secondGameControllerStarted)
        {
            secondGameController.start();
            secondGameControllerStarted = true;
        }
        frame.activateWindow(AppWindowKey.GAME_SECOND);
    }

    @Override
    public void openSettings()
    {
        Logger.debug(LOG_SOURCE, "open_settings", "Opening settings window.");
        settingsWindow.setSelectedLocale(localizationService.getCurrentLocale());
        showWindow(AppWindowKey.SETTINGS);
    }

    @Override
    public void changeLocale(AppLocale locale)
    {
        Logger.info(LOG_SOURCE, "change_locale", String.valueOf(locale));
        localizationService.setCurrentLocale(locale);
        settingsService.saveLocale(localizationService.getCurrentLocale());
        refreshLocalizedUi();
    }

    @Override
    public void appendLogMessage()
    {
        Logger.debug(LOG_SOURCE, "append_log_message", localizationService.get("log.new_line"));
        showWindow(AppWindowKey.LOG);
    }

    @Override
    public void showAbout()
    {
        Logger.debug(LOG_SOURCE, "show_about", "Showing about dialog.");
        frame.showAboutDialog();
    }

    private void refreshLocalizedUi()
    {
        Logger.debug(LOG_SOURCE, "refresh_localized_ui", "Applying locale and refreshing texts.");
        Locale.setDefault(localizationService.getCurrentLocale().toLocale());
        UIManager.put("OptionPane.yesButtonText", localizationService.get("option.yes"));
        UIManager.put("OptionPane.noButtonText", localizationService.get("option.no"));
        UIManager.put("OptionPane.cancelButtonText", localizationService.get("option.cancel"));
        UIManager.put("OptionPane.okButtonText", localizationService.get("option.ok"));
        for (LocalizableView view : localizableViews)
        {
            view.updateTexts();
        }
        frame.applyLookAndFeel(UIManager.getLookAndFeel().getClass().getName());
    }
}
