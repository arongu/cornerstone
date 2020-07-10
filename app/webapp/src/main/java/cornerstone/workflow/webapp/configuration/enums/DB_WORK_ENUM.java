package cornerstone.workflow.webapp.configuration.enums;

public enum DB_WORK_ENUM {
    DB_DRIVER("db_work_driver"),
    DB_URL("db_work_url"),
    DB_USER("db_work_username"),
    DB_PASSWORD("db_work_password"),
    DB_MIN_IDLE("db_work_min_idle"),
    DB_MAX_IDLE("db_work_max_idle"),
    DB_MAX_OPEN("db_work_max_open");

    public static final String PREFIX_DB_WORK = "db_work";
    public final String key;

    DB_WORK_ENUM(final String key) {
        this.key = key;
    }
}
