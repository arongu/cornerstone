package cornerstone.webapp.configuration;

/**
 * The basic configuration default used by the application when deployed.
 * By default this sit the parameters where cornerstone will try to load:
 *  - conf file (holds the properties used at runtime)
 *  - key file  (the key which can be used to decrypt the configuration file)
 */
public final class ConfigDefaults {
    private ConfigDefaults(){};

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
