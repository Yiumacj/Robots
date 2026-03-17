package controller;

import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import gui.GameWindow;
import gui.LogWindow;
import gui.MainApplicationFrame;
import gui.RobotCoordinatesWindow;
import gui.RobotStateView;
import gui.SettingsWindow;
import log.LogWindowSource;
import model.AppLocale;
import model.AppWindowKey;
import service.ApplicationSettingsService;
import service.LocalizationService;
import service.ResourceBundleLocalizationService;

public class DefaultApplicationControllerTest
{
    @Test
    public void showWindowDelegatesToFrame()
    {
        StubMainApplicationFrame frame = new StubMainApplicationFrame();
        LocalizationService localizationService = new ResourceBundleLocalizationService();
        DefaultApplicationController controller = new DefaultApplicationController(
                frame,
                new StubGameController(),
                new LogWindow(new LogWindowSource(10), localizationService),
                new GameWindow(localizationService),
                new RobotCoordinatesWindow(localizationService),
                new SettingsWindow(localizationService),
                localizationService,
                new StubWindowStateService());

        controller.showWindow(AppWindowKey.GAME);

        Assert.assertEquals(AppWindowKey.GAME, frame.lastActivatedWindow);
    }

    @Test
    public void openSettingsUsesSameWindowActivationPath()
    {
        StubMainApplicationFrame frame = new StubMainApplicationFrame();
        LocalizationService localizationService = new ResourceBundleLocalizationService();
        DefaultApplicationController controller = new DefaultApplicationController(
                frame,
                new StubGameController(),
                new LogWindow(new LogWindowSource(10), localizationService),
                new GameWindow(localizationService),
                new RobotCoordinatesWindow(localizationService),
                new SettingsWindow(localizationService),
                localizationService,
                new StubWindowStateService());

        controller.openSettings();

        Assert.assertEquals(AppWindowKey.SETTINGS, frame.lastActivatedWindow);
    }

    @Test
    public void exitSavesStateAndStopsGameLoop()
    {
        StubMainApplicationFrame frame = new StubMainApplicationFrame();
        StubGameController gameController = new StubGameController();
        StubWindowStateService windowStateService = new StubWindowStateService();
        LocalizationService localizationService = new ResourceBundleLocalizationService();
        DefaultApplicationController controller = new DefaultApplicationController(
                frame,
                gameController,
                new LogWindow(new LogWindowSource(10), localizationService),
                new GameWindow(localizationService),
                new RobotCoordinatesWindow(localizationService),
                new SettingsWindow(localizationService),
                localizationService,
                windowStateService);

        controller.exit();

        Assert.assertTrue(gameController.stopCalled);
        Assert.assertTrue(windowStateService.saveCalled);
        Assert.assertTrue(frame.closeCalled);
    }

    @Test
    public void activateWindowReopensClosedInternalFrame() throws Exception
    {
        MainApplicationFrame frame = new MainApplicationFrame(new ResourceBundleLocalizationService());
        javax.swing.JInternalFrame internalFrame = new javax.swing.JInternalFrame();
        frame.addWindow(AppWindowKey.LOG, internalFrame);

        internalFrame.setClosed(true);
        frame.activateWindow(AppWindowKey.LOG);

        Assert.assertFalse(internalFrame.isClosed());
        Assert.assertTrue(internalFrame.isVisible());
    }

    private static class StubMainApplicationFrame extends MainApplicationFrame
    {
        private AppWindowKey lastActivatedWindow;
        private boolean closeCalled;

        private StubMainApplicationFrame()
        {
            super(new ResourceBundleLocalizationService());
        }

        @Override
        public void activateWindow(AppWindowKey key)
        {
            lastActivatedWindow = key;
        }

        @Override
        public boolean confirmExit()
        {
            return true;
        }

        @Override
        public void closeApplication()
        {
            closeCalled = true;
        }
    }

    private static class StubGameController implements GameController
    {
        private boolean stopCalled;

        @Override
        public void start()
        {
        }

        @Override
        public void stop()
        {
            stopCalled = true;
        }

        @Override
        public void addView(RobotStateView view)
        {
        }

        @Override
        public void setTargetPosition(Point point)
        {
        }

        @Override
        public void tick(double duration)
        {
        }
    }

    private static class StubWindowStateService implements ApplicationSettingsService
    {
        private boolean saveCalled;
        private AppLocale savedLocale = AppLocale.RU_RU;

        @Override
        public void loadWindowState(MainApplicationFrame frame)
        {
        }

        @Override
        public void saveWindowState(MainApplicationFrame frame)
        {
            saveCalled = true;
        }

        @Override
        public AppLocale loadLocale()
        {
            return savedLocale;
        }

        @Override
        public void saveLocale(AppLocale locale)
        {
            savedLocale = locale;
        }
    }
}
