package cornerstone.workflow.webapp.configuration.enums;

public enum DB_USERS_ENUM {

    DB_DRIVER("db_users_driver"),
    DB_URL("db_users_url"),
    DB_USERNAME("db_users_username"),
    DB_PASSWORD("db_users_password"),
    DB_MIN_IDLE("db_users_min_idle"),
    DB_MAX_IDLE("db_users_max_idle"),
    DB_MAX_OPEN("db_users_max_open");

    public static final String PREFIX_DB_USERS = "db_users";
    public final String key;

    DB_USERS_ENUM(final String key) {
        this.key = key;
    }
}
