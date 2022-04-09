package cornerstone.webapp.configuration.enums;

/**
 * Fields from configFile used only for configuring the accounts DB.
 * sensitiveValue - set it to true to hide its value in the logs.
 */
public enum DB_ACCOUNTS_ENUM {

    DB_DRIVER("db_accounts_driver", false),
    DB_URL("db_accounts_url", false),
    DB_USERNAME("db_accounts_username", false),
    DB_PASSWORD("db_accounts_password", true),
    DB_MIN_IDLE("db_accounts_min_idle", false),
    DB_MAX_IDLE("db_accounts_max_idle", false),
    DB_MAX_OPEN("db_accounts_max_open", false);

    public static final String PREFIX_DB_ACCOUNTS = "db_accounts";
    public final String key;
    public final boolean sensitiveValue;

    DB_ACCOUNTS_ENUM(final String key, final boolean sensitiveValue) {
        this.key = key;
        this.sensitiveValue = sensitiveValue;
    }
}
