package gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

public class MainApplicationFrame extends JFrame
{
    private static final String CONFIG_FILE_NAME = ".robots.properties";
    private static final String MAIN_WINDOW_KEY = "mainWindow";
    private static final String LOG_WINDOW_KEY = "logWindow";
    private static final String GAME_WINDOW_KEY = "gameWindow";
    private static final String COORDINATES_WINDOW_KEY = "coordinatesWindow";

    private final JDesktopPane desktopPane = new JDesktopPane();
    private final RobotModel robotModel = new RobotModel();
    private final LogWindow logWindow;
    private final GameWindow gameWindow;
    private final RobotCoordinatesWindow coordinatesWindow;

    public MainApplicationFrame()
    {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);

        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow(robotModel);
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        coordinatesWindow = createRobotCoordinatesWindow();
        addWindow(coordinatesWindow);

        restoreWindowGeometry();

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                exitApplication();
            }
        });
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected RobotCoordinatesWindow createRobotCoordinatesWindow()
    {
        RobotCoordinatesWindow window = new RobotCoordinatesWindow(robotModel);
        window.setLocation(420, 10);
        return window;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        return menuBar;
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_A);
        fileMenu.getAccessibleContext().setAccessibleDescription(
                "Команды работы с приложением");

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitItem.addActionListener((event) -> exitApplication());
        fileMenu.add(exitItem);
        return fileMenu;
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        return lookAndFeelMenu;
    }

    private JMenu createTestMenu()
    {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> Logger.debug("Новая строка"));
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    private void exitApplication()
    {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION)
        {
            saveWindowGeometry();
            dispose();
            System.exit(0);
        }
    }

    private void saveWindowGeometry()
    {
        Properties properties = new Properties();
        saveMainWindowGeometry(properties, MAIN_WINDOW_KEY);
        saveInternalWindowGeometry(properties, LOG_WINDOW_KEY, logWindow);
        saveInternalWindowGeometry(properties, GAME_WINDOW_KEY, gameWindow);
        saveInternalWindowGeometry(properties, COORDINATES_WINDOW_KEY, coordinatesWindow);

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

    private void restoreWindowGeometry()
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

        restoreMainWindowGeometry(properties, MAIN_WINDOW_KEY);
        restoreInternalWindowGeometry(properties, LOG_WINDOW_KEY, logWindow);
        restoreInternalWindowGeometry(properties, GAME_WINDOW_KEY, gameWindow);
        restoreInternalWindowGeometry(properties, COORDINATES_WINDOW_KEY, coordinatesWindow);
    }

    private void saveMainWindowGeometry(Properties properties, String key)
    {
        saveBounds(properties, key, getBounds());
        properties.setProperty(key + ".extendedState", Integer.toString(getExtendedState()));
    }

    private void restoreMainWindowGeometry(Properties properties, String key)
    {
        restoreBounds(properties, key, this);
        setExtendedState(readInt(properties, key + ".extendedState", JFrame.NORMAL));
    }

    private void saveInternalWindowGeometry(Properties properties, String key, JInternalFrame frame)
    {
        saveBounds(properties, key, frame.getBounds());
        properties.setProperty(key + ".icon", Boolean.toString(frame.isIcon()));
        properties.setProperty(key + ".maximum", Boolean.toString(frame.isMaximum()));
    }

    private void restoreInternalWindowGeometry(Properties properties, String key, JInternalFrame frame)
    {
        restoreBounds(properties, key, frame);
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

    private void restoreBounds(Properties properties, String key, java.awt.Window window)
    {
        Rectangle bounds = readBounds(properties, key, window.getBounds());
        window.setBounds(bounds);
    }

    private void restoreBounds(Properties properties, String key, JInternalFrame frame)
    {
        Rectangle bounds = readBounds(properties, key, frame.getBounds());
        frame.setBounds(bounds);
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

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }
}