package service;

import model.AppLocale;

public interface LocalizationService
{
    String get(String key);

    AppLocale getCurrentLocale();

    void setCurrentLocale(AppLocale locale);
}
