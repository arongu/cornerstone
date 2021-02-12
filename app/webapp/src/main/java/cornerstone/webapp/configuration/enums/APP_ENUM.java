package cornerstone.webapp.configuration.enums;

/**
 * Fields from configFile used only for configuring the application.
 * sensitiveValue - set it to true to hide its value in the logs.
 */
public enum APP_ENUM {
    APP_NODE_NAME("app_node_name", false),
    APP_RSA_TTL("app_rsa_ttl", false),
    APP_JWT_TTL("app_jwt_ttl", false),
    APP_MAX_LOGIN_ATTEMPTS("app_max_login_attempts", false);

    public static final String PREFIX_APP = "app";
    public final String key;
    public final boolean sensitiveValue;

    APP_ENUM(final String key, final boolean sensitiveValue) {
        this.key = key;
        this.sensitiveValue = sensitiveValue;
    }
}
