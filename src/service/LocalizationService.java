package service;

import model.AppLocale;

public interface LocalizationService
{
    String get(String key);

    String format(String key, Object... arguments);

    AppLocale getCurrentLocale();

    void setCurrentLocale(AppLocale locale);
}
