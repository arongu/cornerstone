package cornerstone.workflow.app.configuration;

public enum DBConfigurationField {
    DB_MAIN_DRIVER("db_main_driver"),
    DB_MAIN_URL("db_main_url"),
    DB_MAIN_USER("db_main_username"),
    DB_MAIN_PASSWORD("db_main_password"),
    DB_MAIN_MIN_IDLE("db_main_min_idle"),
    DB_MAIN_MAX_IDLE("db_main_max_idle"),
    DB_MAIN_MAX_OPEN("db_main_max_open"),

    DB_ADMIN_DRIVER("db_user_driver"),
    DB_ADMIN_URL("db_user_url"),
    DB_ADMIN_USER("db_user_username"),
    DB_ADMIN_PASSWORD("db_user_password"),
    DB_ADMIN_MIN_IDLE("db_user_min_idle"),
    DB_ADMIN_MAX_IDLE("db_user_max_idle"),
    DB_ADMIN_MAX_OPEN("db_user_max_open");

    private String key;

    DBConfigurationField(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static final String mainPrefix = "db_main";
    public static final String userPrefix = "db_user";
}
