package service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import model.AppLocale;

public class ResourceBundleLocalizationService implements LocalizationService
{
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
    }
}
