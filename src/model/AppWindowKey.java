package model;

public enum AppWindowKey
{
    LOG("logWindow"),
    GAME("gameWindow"),
    GAME_SECOND("gameWindow2"),
    COORDINATES("coordinatesWindow"),
    SETTINGS("settingsWindow");

    private final String propertyKey;

    AppWindowKey(String propertyKey)
    {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }
}
