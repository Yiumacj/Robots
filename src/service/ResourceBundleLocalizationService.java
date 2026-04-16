package service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import log.Logger;
import model.AppLocale;

public class ResourceBundleLocalizationService implements LocalizationService
{
    private static final String LOG_SOURCE = "service.ResourceBundleLocalizationService";
    private static final String BUNDLE_NAME = "i18n.messages";

    private AppLocale currentLocale = AppLocale.RU_RU;
    private ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale.toLocale());

    @Override
    public String get(String key)
    {
        try
        {
            return bundle.getString(key);
        }
        catch (MissingResourceException ex)
        {
            Logger.warn(LOG_SOURCE, "missing_key", "Missing translation key '" + key + "' for locale " + currentLocale);
            ResourceBundle fallbackBundle = ResourceBundle.getBundle(BUNDLE_NAME, AppLocale.RU_RU.toLocale());
            return fallbackBundle.containsKey(key) ? fallbackBundle.getString(key) : key;
        }
    }

    @Override
    public AppLocale getCurrentLocale()
    {
        return currentLocale;
    }

    @Override
    public void setCurrentLocale(AppLocale locale)
    {
        currentLocale = locale == null ? AppLocale.RU_RU : locale;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale.toLocale());
        Logger.info(LOG_SOURCE, "set_locale", "Locale switched to " + currentLocale);
    }
}
