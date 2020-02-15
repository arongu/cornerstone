package cornerstone.workflow.app.configuration.enums;

public enum ConfigFieldsApp {

    APP_ADMIN_USER("app_admin_user"),
    APP_JWS_KEY("app_jws_key");

    public static final String prefix_app = "app";

    public final String key;

    ConfigFieldsApp(final String key){
        this.key = key;
    }
}
