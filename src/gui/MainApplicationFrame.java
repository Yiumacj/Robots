package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.EnumMap;
import java.util.Map;

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

import controller.ApplicationController;
import model.AppWindowKey;
import service.LocalizationService;

public class MainApplicationFrame extends JFrame implements LocalizableView
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final Map<AppWindowKey, JInternalFrame> windows = new EnumMap<AppWindowKey, JInternalFrame>(AppWindowKey.class);
    private final LocalizationService localizationService;
    private final DesktopLauncherPanel desktopLauncherPanel;
    private ApplicationController controller;

    public MainApplicationFrame(LocalizationService localizationService)
    {
        this.localizationService = localizationService;
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        desktopPane.setBackground(new Color(221, 232, 244));
        desktopLauncherPanel = new DesktopLauncherPanel(localizationService);
        desktopLauncherPanel.setBounds(24, 24, 360, 220);
        desktopPane.add(desktopLauncherPanel, JDesktopPane.DEFAULT_LAYER);
        setContentPane(desktopPane);
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (controller != null)
                {
                    controller.exit();
                }
            }
        });
    }

    public void setController(ApplicationController controller)
    {
        this.controller = controller;
        desktopLauncherPanel.setController(controller);
    }

    public void addWindow(AppWindowKey key, JInternalFrame frame)
    {
        windows.put(key, frame);
        frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        desktopPane.add(frame, JDesktopPane.PALETTE_LAYER);
        frame.setVisible(true);
    }

    public JInternalFrame getWindow(AppWindowKey key)
    {
        return windows.get(key);
    }

    public void activateWindow(AppWindowKey key)
    {
        JInternalFrame frame = windows.get(key);
        if (frame == null)
        {
            return;
        }

        frame.setVisible(true);
        try
        {
            if (frame.isClosed())
            {
                frame.setClosed(false);
            }
            frame.setIcon(false);
            frame.setSelected(true);
            frame.toFront();
        }
        catch (PropertyVetoException ignored)
        {
            // Visibility is enough if focus cannot be changed.
        }
    }

    public boolean confirmExit()
    {
        int result = JOptionPane.showConfirmDialog(
                this,
                localizationService.get("dialog.exit.message"),
                localizationService.get("dialog.exit.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public void showAboutDialog()
    {
        JOptionPane.showMessageDialog(
                this,
                localizationService.get("dialog.about.message"),
                localizationService.get("dialog.about.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void closeApplication()
    {
        dispose();
        System.exit(0);
    }

    public void applyLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // Ignore unsupported themes and keep the current one.
        }
    }

    @Override
    public void updateTexts()
    {
        setTitle(localizationService.get("app.title"));
        desktopLauncherPanel.updateTexts();
        setJMenuBar(generateMenuBar());
        revalidate();
        repaint();
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createWindowsMenu());
        menuBar.add(createControlMenu());
        menuBar.add(createSettingsMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu(localizationService.get("menu.file"));
        fileMenu.setMnemonic(KeyEvent.VK_A);

        JMenuItem exitItem = new JMenuItem(localizationService.get("menu.file.exit"), KeyEvent.VK_X);
        exitItem.addActionListener((event) -> {
            if (controller != null)
            {
                controller.exit();
            }
        });
        fileMenu.add(exitItem);
        return fileMenu;
    }

    private JMenu createViewMenu()
    {
        JMenu viewMenu = new JMenu(localizationService.get("menu.view"));
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem systemLookAndFeel = new JMenuItem(localizationService.get("menu.view.system"), KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            if (controller != null)
            {
                controller.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        });
        viewMenu.add(systemLookAndFeel);

        JMenuItem crossPlatformLookAndFeel = new JMenuItem(localizationService.get("menu.view.cross_platform"), KeyEvent.VK_U);
        crossPlatformLookAndFeel.addActionListener((event) -> {
            if (controller != null)
            {
                controller.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        });
        viewMenu.add(crossPlatformLookAndFeel);

        JMenuItem nimbusLookAndFeel = new JMenuItem(localizationService.get("menu.view.nimbus"), KeyEvent.VK_N);
        nimbusLookAndFeel.addActionListener((event) -> {
            if (controller != null)
            {
                controller.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
        });
        viewMenu.add(nimbusLookAndFeel);

        return viewMenu;
    }

    private JMenu createWindowsMenu()
    {
        JMenu windowsMenu = new JMenu(localizationService.get("menu.windows"));
        windowsMenu.setMnemonic(KeyEvent.VK_W);
        windowsMenu.add(createWindowItem(localizationService.get("menu.windows.game"), AppWindowKey.GAME));
        windowsMenu.add(createWindowItem(localizationService.get("menu.windows.coordinates"), AppWindowKey.COORDINATES));
        windowsMenu.add(createWindowItem(localizationService.get("menu.windows.log"), AppWindowKey.LOG));
        windowsMenu.add(createWindowItem(localizationService.get("menu.windows.settings"), AppWindowKey.SETTINGS));
        return windowsMenu;
    }

    private JMenuItem createWindowItem(String title, AppWindowKey key)
    {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener((event) -> {
            if (controller != null)
            {
                controller.showWindow(key);
            }
        });
        return item;
    }

    private JMenu createControlMenu()
    {
        JMenu controlMenu = new JMenu(localizationService.get("menu.control"));
        controlMenu.setMnemonic(KeyEvent.VK_T);

        JMenuItem addLogMessageItem = new JMenuItem(localizationService.get("menu.control.add_log"), KeyEvent.VK_L);
        addLogMessageItem.addActionListener((event) -> {
            if (controller != null)
            {
                controller.appendLogMessage();
            }
        });
        controlMenu.add(addLogMessageItem);
        return controlMenu;
    }

    private JMenu createSettingsMenu()
    {
        JMenu settingsMenu = new JMenu(localizationService.get("menu.settings"));
        settingsMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem openSettingsItem = new JMenuItem(localizationService.get("menu.settings.open"));
        openSettingsItem.addActionListener((event) -> {
            if (controller != null)
            {
                controller.openSettings();
            }
        });
        settingsMenu.add(openSettingsItem);
        return settingsMenu;
    }

    private JMenu createHelpMenu()
    {
        JMenu helpMenu = new JMenu(localizationService.get("menu.help"));
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem(localizationService.get("menu.help.about"), KeyEvent.VK_A);
        aboutItem.addActionListener((event) -> {
            if (controller != null)
            {
                controller.showAbout();
            }
        });
        helpMenu.add(aboutItem);
        return helpMenu;
    }
}
