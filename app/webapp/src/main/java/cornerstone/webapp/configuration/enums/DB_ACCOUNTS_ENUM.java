package cornerstone.webapp.configuration.enums;

/**
 * Fields from configFile used only for configuring the users DB.
 * sensitiveValue - set it to true to hide its value in the logs.
 */
public enum DB_USERS_ENUM {

    DB_DRIVER("db_users_driver", false),
    DB_URL("db_users_url", false),
    DB_USERNAME("db_users_username", false),
    DB_PASSWORD("db_users_password", true),
    DB_MIN_IDLE("db_users_min_idle", false),
    DB_MAX_IDLE("db_users_max_idle", false),
    DB_MAX_OPEN("db_users_max_open", false);

    public static final String PREFIX_DB_USERS = "db_users";
    public final String key;
    public final boolean sensitiveValue;

    DB_USERS_ENUM(final String key, final boolean sensitiveValue) {
        this.key = key;
        this.sensitiveValue = sensitiveValue;
    }
}
