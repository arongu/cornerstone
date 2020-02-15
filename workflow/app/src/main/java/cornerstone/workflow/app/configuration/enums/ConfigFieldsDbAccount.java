package cornerstone.workflow.app.configuration.enums;

public enum ConfigFieldsDbAccount {

    DB_ACCOUNT_DRIVER("db_account_driver"),
    DB_ACCOUNT_URL("db_account_url"),
    DB_ACCOUNT_USER("db_account_username"),
    DB_ACCOUNT_PASSWORD("db_account_password"),
    DB_ACCOUNT_MIN_IDLE("db_account_min_idle"),
    DB_ACCOUNT_MAX_IDLE("db_account_max_idle"),
    DB_ACCOUNT_MAX_OPEN("db_account_max_open");

    public static final String prefix_db_account = "db_account";
    public final String key;

    ConfigFieldsDbAccount(final String key) {
        this.key = key;
    }
}
