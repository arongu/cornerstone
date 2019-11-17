package cornerstone.workflow.app.configuration;

public enum DBConfigurationField {
    DB_DATA_DRIVER("db_data_driver"),
    DB_DATA_URL("db_data_url"),
    DB_DATA_USER("db_data_username"),
    DB_DATA_PASSWORD("db_data_password"),
    DB_DATA_MIN_IDLE("db_data_min_idle"),
    DB_DATA_MAX_IDLE("db_data_max_idle"),
    DB_DATA_MAX_OPEN("db_data_max_open"),

    DB_ACCOUNT_DRIVER("db_account_driver"),
    DB_ACCOUNT_URL("db_account_url"),
    DB_ACCOUNT_USER("db_account_username"),
    DB_ACCOUNT_PASSWORD("db_account_password"),
    DB_ACCOUNT_MIN_IDLE("db_account_min_idle"),
    DB_ACCOUNT_MAX_IDLE("db_account_max_idle"),
    DB_ACCOUNT_MAX_OPEN("db_account_max_open");

    private String key;

    DBConfigurationField(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static final String mainPrefix = "db_data";
    public static final String userPrefix = "db_account";
}
