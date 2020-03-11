package cornerstone.workflow.webapp.configuration.enums;

public enum ApplicationConfigFields {

    APP_ADMIN_USER("app_admin_user"),
    APP_JWS_KEY("app_jws_key");

    public static final String prefix_app = "app";

    public final String key;

    ApplicationConfigFields(final String key){
        this.key = key;
    }
}
