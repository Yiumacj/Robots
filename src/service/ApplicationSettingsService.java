package service;

import model.AppLocale;

public interface ApplicationSettingsService extends WindowStateService
{
    AppLocale loadLocale();

    void saveLocale(AppLocale locale);
}
