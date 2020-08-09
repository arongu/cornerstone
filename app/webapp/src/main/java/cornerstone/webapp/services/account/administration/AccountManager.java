package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import cornerstone.webapp.services.account.administration.exceptions.*;

import java.util.List;
import java.util.NoSuchElementException;

public interface AccountManager {
    // Create
    int create(final String email, final String password, final boolean locked, final boolean verified) throws AccountManagerSqlException;
    int create(final List<AccountEmailPassword> list)                                                   throws AccountManagerSqlException, AccountManagerSqlBulkException;

    // Read
    AccountResultSet get(final String email) throws AccountManagerSqlException, NoSuchElementException;

    // Update
    int setPassword    (final String email, final String password)        throws AccountManagerSqlException;
    int setEmail       (final String currentEmail, final String newEmail) throws AccountManagerSqlException;

    int incrementLoginAttempts(final String email) throws AccountManagerSqlException;
    int clearLoginAttempts    (final String email) throws AccountManagerSqlException;

    int lock  (final String email, final String reason) throws AccountManagerSqlException;
    int unlock(final String email)                      throws AccountManagerSqlException;

    // Delete
    int delete(final String email)        throws AccountManagerSqlException;
    int delete(final List<String> emails) throws AccountManagerSqlException, AccountManagerSqlBulkException;

    // Login
    boolean login(final String email, final String password) throws AccountManagerSqlException,
                                                                    AccountManagerAccountLockedException,
                                                                    AccountManagerEmailNotVerifiedException,
                                                                    AccountManagerAccountDoesNotExistException;
}
