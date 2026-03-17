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
    private final MainApplicationFrame frame;
    private final GameController gameController;
    private final LogWindow logWindow;
    private final GameWindow gameWindow;
    private final RobotCoordinatesWindow coordinatesWindow;
    private final SettingsWindow settingsWindow;
    private final LocalizationService localizationService;
    private final ApplicationSettingsService settingsService;
    private final LocalizableView[] localizableViews;

    public DefaultApplicationController(MainApplicationFrame frame,
                                        GameController gameController,
                                        LogWindow logWindow,
                                        GameWindow gameWindow,
                                        RobotCoordinatesWindow coordinatesWindow,
                                        SettingsWindow settingsWindow,
                                        LocalizationService localizationService,
                                        ApplicationSettingsService settingsService)
    {
        this.frame = frame;
        this.gameController = gameController;
        this.logWindow = logWindow;
        this.gameWindow = gameWindow;
        this.coordinatesWindow = coordinatesWindow;
        this.settingsWindow = settingsWindow;
        this.localizationService = localizationService;
        this.settingsService = settingsService;
        this.localizableViews = new LocalizableView[] { frame, logWindow, gameWindow, coordinatesWindow, settingsWindow };
    }

    @Override
    public void start()
    {
        frame.addWindow(AppWindowKey.LOG, logWindow);
        frame.addWindow(AppWindowKey.GAME, gameWindow);
        frame.addWindow(AppWindowKey.COORDINATES, coordinatesWindow);
        frame.addWindow(AppWindowKey.SETTINGS, settingsWindow);
        settingsWindow.setVisible(false);
        frame.setMinimumSize(logWindow.getSize());
        settingsWindow.setSelectedLocale(localizationService.getCurrentLocale());
        settingsService.loadWindowState(frame);
        refreshLocalizedUi();
        Logger.debug(localizationService.get("log.protocol_started"));
        gameController.start();
    }

    @Override
    public void exit()
    {
        if (!frame.confirmExit())
        {
            return;
        }
        gameController.stop();
        settingsService.saveWindowState(frame);
        frame.closeApplication();
    }

    @Override
    public void setLookAndFeel(String className)
    {
        frame.applyLookAndFeel(className);
        frame.invalidate();
    }

    @Override
    public void showWindow(AppWindowKey key)
    {
        frame.activateWindow(key);
    }

    @Override
    public void openSettings()
    {
        settingsWindow.setSelectedLocale(localizationService.getCurrentLocale());
        showWindow(AppWindowKey.SETTINGS);
    }

    @Override
    public void changeLocale(AppLocale locale)
    {
        localizationService.setCurrentLocale(locale);
        settingsService.saveLocale(localizationService.getCurrentLocale());
        refreshLocalizedUi();
    }

    @Override
    public void appendLogMessage()
    {
        Logger.debug(localizationService.get("log.new_line"));
        showWindow(AppWindowKey.LOG);
    }

    @Override
    public void showAbout()
    {
        frame.showAboutDialog();
    }

    private void refreshLocalizedUi()
    {
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
