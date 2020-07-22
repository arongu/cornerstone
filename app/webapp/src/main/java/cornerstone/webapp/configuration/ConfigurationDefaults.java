package cornerstone.webapp.configuration;

public final class ConfigurationDefaults {
    private ConfigurationDefaults(){};

    // Environment variables
    public static final String SYSTEM_PROPERTY_KEY_FILE  = "KEY_FILE";
    public static final String SYSTEM_PROPERTY_CONF_FILE = "CONF_FILE";

    // Configuration path
    public static final String DEFAULT_PATH  = "/opt/cornerstone/";
    public static final String CONF_FILENAME = "app.conf";
    public static final String KEY_FILENAME  = "key.conf";

    public static final String DEFAULT_CONF_FILE = DEFAULT_PATH + CONF_FILENAME;
    public static final String DEFAULT_KEY_FILE  = DEFAULT_PATH + KEY_FILENAME;
}
