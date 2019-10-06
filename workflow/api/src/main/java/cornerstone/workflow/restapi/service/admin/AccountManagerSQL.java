package cornerstone.workflow.restapi.service.admin;

/* The SQL queries used for managing accounts. */
public final class AccountManagerSQL {
    static final String SQL_CREATE_ACCOUNT = "INSERT INTO accounts (password_hash, email_address, account_enabled, email_address_verified) VALUES(?,?,?,?)";
    static final String SQL_DELETE_ACCOUNT = "DELETE FROM accounts WHERE email_address=(?)";
}
