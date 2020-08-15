package cornerstone.webapp.config.enums;

public enum APP_ENUM {
    APP_NODE_NAME("app_node_name"),
    APP_RSA_TTL("app_rsa_ttl"),
    APP_JWT_TTL("app_jwt_ttl"),
    APP_MAX_LOGIN_ATTEMPTS("app_max_login_attempts");

    public static final String PREFIX_APP = "app";
    public final String key;

    APP_ENUM(final String key) {
        this.key = key;
    }
}
