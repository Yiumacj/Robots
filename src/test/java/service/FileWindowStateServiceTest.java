package service;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JInternalFrame;

import org.junit.Assert;
import org.junit.Test;

import gui.MainApplicationFrame;
import service.ResourceBundleLocalizationService;
import model.AppWindowKey;

public class FileWindowStateServiceTest
{
    @Test
    public void savesAndLoadsWindowBounds()
    {
        FileWindowStateService service = new FileWindowStateService();
        TestMainApplicationFrame frame = new TestMainApplicationFrame();
        frame.setBounds(1, 2, 3, 4);

        JInternalFrame gameFrame = new JInternalFrame();
        gameFrame.setBounds(10, 20, 30, 40);
        frame.addWindow(AppWindowKey.GAME, gameFrame);

        service.saveWindowState(frame);

        frame.setBounds(100, 200, 300, 400);
        gameFrame.setBounds(50, 60, 70, 80);
        service.loadWindowState(frame);

        Assert.assertEquals(new Rectangle(1, 2, 3, 4), frame.getBounds());
        Assert.assertEquals(new Rectangle(10, 20, 30, 40), gameFrame.getBounds());
    }

    @Test
    public void brokenGeometryFallsBackToDefaults() throws Exception
    {
        File configFile = new File(System.getProperty("user.home"), ".robots.properties");
        Properties properties = new Properties();
        properties.setProperty("mainWindow.x", "broken");
        try (FileOutputStream outputStream = new FileOutputStream(configFile))
        {
            properties.store(outputStream, "broken");
        }

        FileWindowStateService service = new FileWindowStateService();
        TestMainApplicationFrame frame = new TestMainApplicationFrame();
        frame.setBounds(7, 8, 9, 10);

        service.loadWindowState(frame);

        Assert.assertEquals(new Rectangle(7, 8, 9, 10), frame.getBounds());
    }

    private static class TestMainApplicationFrame extends MainApplicationFrame
    {
        private TestMainApplicationFrame()
        {
            super(new ResourceBundleLocalizationService());
        }
    }
}
