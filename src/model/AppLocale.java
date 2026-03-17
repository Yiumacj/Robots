package model;

import java.util.Locale;

public enum AppLocale
{
    RU_RU("ru-RU", new Locale("ru", "RU")),
    EN_US("en-US", new Locale("en", "US"));

    private final String code;
    private final Locale locale;

    AppLocale(String code, Locale locale)
    {
        this.code = code;
        this.locale = locale;
    }

    public String getCode()
    {
        return code;
    }

    public Locale toLocale()
    {
        return locale;
    }

    public static AppLocale fromCode(String code)
    {
        if (code != null)
        {
            for (AppLocale locale : values())
            {
                if (locale.code.equalsIgnoreCase(code))
                {
                    return locale;
                }
            }
        }
        return RU_RU;
    }
}
