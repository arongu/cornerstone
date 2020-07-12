package cornerstone.workflow.webapp.configuration.enums;

public enum APP_ENUM {
    NODE_NAME("app_node_name");

    public static final String PREFIX_APP = "app";
    public final String key;

    APP_ENUM(final String key) {
            this.key = key;
    }
}
