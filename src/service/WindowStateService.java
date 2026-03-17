package service;

import gui.MainApplicationFrame;

public interface WindowStateService
{
    void loadWindowState(MainApplicationFrame frame);

    void saveWindowState(MainApplicationFrame frame);
}
