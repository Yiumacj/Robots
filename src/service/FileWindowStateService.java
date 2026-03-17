package service;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import gui.MainApplicationFrame;
import log.Logger;
import model.AppLocale;
import model.AppWindowKey;

public class FileWindowStateService implements ApplicationSettingsService
{
    private static final String CONFIG_FILE_NAME = ".robots.properties";
    private static final String MAIN_WINDOW_KEY = "mainWindow";
    private static final String LOCALE_KEY = "app.locale";

    @Override
    public void loadWindowState(MainApplicationFrame frame)
    {
        File configFile = getConfigFile();
        if (!configFile.exists())
        {
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFile))
        {
            properties.load(inputStream);
        }
        catch (IOException e)
        {
            Logger.error("Не удалось загрузить геометрию окон: " + e.getMessage());
            return;
        }

        restoreMainWindowGeometry(properties, frame);
        for (AppWindowKey key : AppWindowKey.values())
        {
            JInternalFrame internalFrame = frame.getWindow(key);
            if (internalFrame != null)
            {
                restoreInternalWindowGeometry(properties, key.getPropertyKey(), internalFrame);
            }
        }
    }

    @Override
    public void saveWindowState(MainApplicationFrame frame)
    {
        Properties properties = loadProperties();
        saveMainWindowGeometry(properties, frame);
        for (AppWindowKey key : AppWindowKey.values())
        {
            JInternalFrame internalFrame = frame.getWindow(key);
            if (internalFrame != null)
            {
                saveInternalWindowGeometry(properties, key.getPropertyKey(), internalFrame);
            }
        }

        File configFile = getConfigFile();
        try (FileOutputStream outputStream = new FileOutputStream(configFile))
        {
            properties.store(outputStream, "Robots application window state");
        }
        catch (IOException e)
        {
            Logger.error("Не удалось сохранить геометрию окон: " + e.getMessage());
        }
    }

    @Override
    public AppLocale loadLocale()
    {
        Properties properties = loadProperties();
        return AppLocale.fromCode(properties.getProperty(LOCALE_KEY, AppLocale.RU_RU.getCode()));
    }

    @Override
    public void saveLocale(AppLocale locale)
    {
        Properties properties = loadProperties();
        properties.setProperty(LOCALE_KEY, locale == null ? AppLocale.RU_RU.getCode() : locale.getCode());
        storeProperties(properties);
    }

    private void saveMainWindowGeometry(Properties properties, MainApplicationFrame frame)
    {
        saveBounds(properties, MAIN_WINDOW_KEY, frame.getBounds());
        properties.setProperty(MAIN_WINDOW_KEY + ".extendedState", Integer.toString(frame.getExtendedState()));
    }

    private void restoreMainWindowGeometry(Properties properties, MainApplicationFrame frame)
    {
        frame.setBounds(readBounds(properties, MAIN_WINDOW_KEY, frame.getBounds()));
        frame.setExtendedState(readInt(properties, MAIN_WINDOW_KEY + ".extendedState", JFrame.NORMAL));
    }

    private void saveInternalWindowGeometry(Properties properties, String key, JInternalFrame frame)
    {
        saveBounds(properties, key, frame.getBounds());
        properties.setProperty(key + ".icon", Boolean.toString(frame.isIcon()));
        properties.setProperty(key + ".maximum", Boolean.toString(frame.isMaximum()));
    }

    private void restoreInternalWindowGeometry(Properties properties, String key, JInternalFrame frame)
    {
        frame.setBounds(readBounds(properties, key, frame.getBounds()));
        try
        {
            frame.setIcon(Boolean.parseBoolean(properties.getProperty(key + ".icon", "false")));
            frame.setMaximum(Boolean.parseBoolean(properties.getProperty(key + ".maximum", "false")));
        }
        catch (PropertyVetoException e)
        {
            Logger.error("Не удалось восстановить состояние окна '" + frame.getTitle() + "': " + e.getMessage());
        }
    }

    private void saveBounds(Properties properties, String key, Rectangle bounds)
    {
        properties.setProperty(key + ".x", Integer.toString(bounds.x));
        properties.setProperty(key + ".y", Integer.toString(bounds.y));
        properties.setProperty(key + ".width", Integer.toString(bounds.width));
        properties.setProperty(key + ".height", Integer.toString(bounds.height));
    }

    private Rectangle readBounds(Properties properties, String key, Rectangle defaultBounds)
    {
        int x = readInt(properties, key + ".x", defaultBounds.x);
        int y = readInt(properties, key + ".y", defaultBounds.y);
        int width = readInt(properties, key + ".width", defaultBounds.width);
        int height = readInt(properties, key + ".height", defaultBounds.height);
        return new Rectangle(x, y, width, height);
    }

    private int readInt(Properties properties, String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(defaultValue)));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private File getConfigFile()
    {
        return new File(System.getProperty("user.home"), CONFIG_FILE_NAME);
    }

    private Properties loadProperties()
    {
        Properties properties = new Properties();
        File configFile = getConfigFile();
        if (!configFile.exists())
        {
            return properties;
        }
        try (FileInputStream inputStream = new FileInputStream(configFile))
        {
            properties.load(inputStream);
        }
        catch (IOException e)
        {
            Logger.error("Failed to load application settings: " + e.getMessage());
        }
        return properties;
    }

    private void storeProperties(Properties properties)
    {
        File configFile = getConfigFile();
        try (FileOutputStream outputStream = new FileOutputStream(configFile))
        {
            properties.store(outputStream, "Robots application window state");
        }
        catch (IOException e)
        {
            Logger.error("Failed to store application settings: " + e.getMessage());
        }
    }
}
