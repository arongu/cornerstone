package cornerstone.workflow.webapp.configuration.enums;

public enum APP_ENUM {
    APP_NODE_NAME("app_node_name"),
    APP_RSA_KEY_TTL("app_rsa_key_ttl");
    // add configurable LOGIN ATTEMPTS -- instead of hard coding it

    public static final String PREFIX_APP = "app";
    public final String key;

    APP_ENUM(final String key) {
        this.key = key;
    }
}
