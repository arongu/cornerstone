package cornerstone.workflow.app.configuration.enums;

public enum ConfigFieldsDbData {

    DB_DATA_DRIVER("db_data_driver"),
    DB_DATA_URL("db_data_url"),
    DB_DATA_USER("db_data_username"),
    DB_DATA_PASSWORD("db_data_password"),
    DB_DATA_MIN_IDLE("db_data_min_idle"),
    DB_DATA_MAX_IDLE("db_data_max_idle"),
    DB_DATA_MAX_OPEN("db_data_max_open");

    public static final String prefix_db_main = "db_data";
    public final String key;

    ConfigFieldsDbData(final String key) {
        this.key = key;
    }
}
