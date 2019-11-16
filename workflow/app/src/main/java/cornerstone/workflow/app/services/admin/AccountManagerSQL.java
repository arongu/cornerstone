package cornerstone.workflow.app.services.admin;

/* The SQL queries used for managing accounts. */
public final class AccountManagerSQL {
    static final String SQL_CREATE_ACCOUNT = "INSERT INTO users (password_hash, email_address, account_enabled, email_address_verified) VALUES(?,?,?,?)";
    static final String SQL_DELETE_ACCOUNT = "DELETE FROM users WHERE email_address=(?)";
}
