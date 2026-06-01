package controller;

import model.AppLocale;
import model.AppWindowKey;

public interface ApplicationController
{
    void start();

    void exit();

    void setLookAndFeel(String className);

    void showWindow(AppWindowKey key);

    void openSecondRobotWindow();

    void openSettings();

    void changeLocale(AppLocale locale);

    void appendLogMessage();

    void showAbout();
}
