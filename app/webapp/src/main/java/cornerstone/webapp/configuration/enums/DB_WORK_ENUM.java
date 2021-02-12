package cornerstone.webapp.configuration.enums;

/**
 * Fields from configFile used only for configuring the work DB.
 * sensitiveValue - set it to true to hide its value in the logs.
 */
public enum DB_WORK_ENUM {
    DB_DRIVER("db_work_driver", false),
    DB_URL("db_work_url", false),
    DB_USERNAME("db_work_username", false),
    DB_PASSWORD("db_work_password", true),
    DB_MIN_IDLE("db_work_min_idle", false),
    DB_MAX_IDLE("db_work_max_idle", false),
    DB_MAX_OPEN("db_work_max_open", false);

    public static final String PREFIX_DB_WORK = "db_work";
    public final String key;
    public final boolean sensitiveValue;

    DB_WORK_ENUM(final String key, final boolean sensitiveValue) {
        this.key = key;
        this.sensitiveValue = sensitiveValue;
    }
}
