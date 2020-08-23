package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import cornerstone.webapp.service.account.administration.exceptions.*;

import java.util.List;

public interface AccountManager {
    // Create
    int create(final String email, final String password, final boolean locked, final boolean verified) throws AccountManagerSqlException, AccountCreationException;
    int create(final List<AccountEmailPassword> list) throws AccountManagerSqlException, AccountManagerBulkException, DBConnectionException;

    // Read
    AccountResultSet get(final String email) throws AccountManagerSqlException, AccountDoesNotExistException, AccountReadException;

    // Update
    int setPassword    (final String email, final String password)        throws AccountManagerSqlException;
    int setEmail       (final String currentEmail, final String newEmail) throws AccountManagerSqlException;

    int incrementLoginAttempts(final String email) throws AccountManagerSqlException;
    int clearLoginAttempts    (final String email) throws AccountManagerSqlException;

    int lock  (final String email, final String reason) throws AccountManagerSqlException;
    int unlock(final String email)                      throws AccountManagerSqlException;

    // Delete
    int delete(final String email)        throws AccountManagerSqlException;
    int delete(final List<String> emails) throws AccountManagerSqlException, AccountManagerBulkException;

    // Login
    boolean login(final String email, final String password) throws AccountManagerSqlException,
            AccountLockedException,
            AccountEmailNotVerifiedException,
            AccountDoesNotExistException;
}
