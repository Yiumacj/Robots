package gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import controller.ApplicationController;
import model.AppWindowKey;
import service.LocalizationService;

public class DesktopLauncherPanel extends JPanel implements LocalizableView
{
    private final LocalizationService localizationService;
    private final Map<AppWindowKey, JButton> launchButtons = new LinkedHashMap<AppWindowKey, JButton>();
    private ApplicationController controller;

    public DesktopLauncherPanel(LocalizationService localizationService)
    {
        this.localizationService = localizationService;
        setOpaque(false);
        setLayout(new GridLayout(2, 2, 16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        addButton(AppWindowKey.GAME);
        addButton(AppWindowKey.COORDINATES);
        addButton(AppWindowKey.LOG);
        addButton(AppWindowKey.SETTINGS);
        updateTexts();
    }

    public void setController(ApplicationController controller)
    {
        this.controller = controller;
    }

    @Override
    public void updateTexts()
    {
        updateButtonText(AppWindowKey.GAME, localizationService.get("desktop.tile.game"));
        updateButtonText(AppWindowKey.COORDINATES, localizationService.get("desktop.tile.coordinates"));
        updateButtonText(AppWindowKey.LOG, localizationService.get("desktop.tile.log"));
        updateButtonText(AppWindowKey.SETTINGS, localizationService.get("desktop.tile.settings"));
    }

    private void addButton(final AppWindowKey key)
    {
        JButton button = new JButton();
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 245, 245));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 170, 170)),
                BorderFactory.createEmptyBorder(18, 12, 18, 12)));
        button.addActionListener(event -> {
            if (controller != null)
            {
                controller.showWindow(key);
            }
        });
        launchButtons.put(key, button);
        add(button);
    }

    private void updateButtonText(AppWindowKey key, String text)
    {
        JButton button = launchButtons.get(key);
        if (button != null)
        {
            button.setText("<html><center>" + text + "</center></html>");
        }
    }
}
