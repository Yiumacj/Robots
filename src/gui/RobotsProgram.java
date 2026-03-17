package gui;

import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.ApplicationController;
import controller.DefaultApplicationController;
import controller.DefaultGameController;
import log.Logger;
import model.AppLocale;
import model.RobotModel;
import service.ApplicationSettingsService;
import service.FileWindowStateService;
import service.LocalizationService;
import service.ResourceBundleLocalizationService;

public class RobotsProgram
{
    public static void main(String[] args)
    {
        try
        {
            configureRussianLocale();
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ApplicationSettingsService settingsService = new FileWindowStateService();
            LocalizationService localizationService = new ResourceBundleLocalizationService();
            AppLocale locale = settingsService.loadLocale();
            localizationService.setCurrentLocale(locale);
            configureOptionPaneTexts(localizationService);

            MainApplicationFrame frame = new MainApplicationFrame(localizationService);
            RobotModel robotModel = new RobotModel();
            DefaultGameController gameController = new DefaultGameController(robotModel);

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
        });
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
}
