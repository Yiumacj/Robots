package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.ApplicationController;
import model.AppLocale;
import service.LocalizationService;

public class SettingsWindow extends JInternalFrame implements LocalizableView
{
    private final LocalizationService localizationService;
    private final JComboBox<AppLocale> localeComboBox = new JComboBox<AppLocale>(AppLocale.values());
    private final JLabel languageLabel = new JLabel();
    private final JButton applyButton = new JButton();
    private ApplicationController controller;

    public SettingsWindow(LocalizationService localizationService)
    {
        super("", true, true, true, true);
        this.localizationService = localizationService;

        JPanel content = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(languageLabel);
        form.add(localeComboBox);
        content.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(applyButton);
        content.add(actions, BorderLayout.SOUTH);
        getContentPane().add(content);

        applyButton.addActionListener(event -> {
            if (controller != null)
            {
                controller.changeLocale((AppLocale) localeComboBox.getSelectedItem());
            }
        });

        setSize(320, 140);
        updateTexts();
    }

    public void setController(ApplicationController controller)
    {
        this.controller = controller;
    }

    public void setSelectedLocale(AppLocale locale)
    {
        localeComboBox.setSelectedItem(locale);
    }

    @Override
    public void updateTexts()
    {
        setTitle(localizationService.get("window.settings"));
        languageLabel.setText(localizationService.get("settings.language"));
        applyButton.setText(localizationService.get("settings.apply"));
        localeComboBox.removeAllItems();
        localeComboBox.addItem(AppLocale.RU_RU);
        localeComboBox.addItem(AppLocale.EN_US);
        localeComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(localizationService.get(value == AppLocale.EN_US ? "settings.locale.en" : "settings.locale.ru")));
        localeComboBox.setSelectedItem(localizationService.getCurrentLocale());
    }
}
