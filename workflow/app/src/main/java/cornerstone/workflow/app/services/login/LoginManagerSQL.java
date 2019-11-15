package cornerstone.workflow.app.services.login;

public class LoginManagerSQL {
    static final String SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD = "SELECT account_enabled, password_hash FROM accounts WHERE email_address=(?)";
}
