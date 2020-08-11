package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import cornerstone.webapp.service.account.administration.exceptions.*;

import java.util.List;

public interface AccountManager {
    // Create
    int create(final String email, final String password, final boolean locked, final boolean verified) throws SqlException;
    int create(final List<AccountEmailPassword> list)                                                   throws SqlException, SqlBulkException;

    // Read
    AccountResultSet get(final String email) throws SqlException, AccountDoesNotExistException;

    // Update
    int setPassword    (final String email, final String password)        throws SqlException;
    int setEmail       (final String currentEmail, final String newEmail) throws SqlException;

    int incrementLoginAttempts(final String email) throws SqlException;
    int clearLoginAttempts    (final String email) throws SqlException;

    int lock  (final String email, final String reason) throws SqlException;
    int unlock(final String email)                      throws SqlException;

    // Delete
    int delete(final String email)        throws SqlException;
    int delete(final List<String> emails) throws SqlException, SqlBulkException;

    // Login
    boolean login(final String email, final String password) throws SqlException,
            AccountLockedException,
            AccountEmailNotVerifiedException,
            AccountDoesNotExistException;
}
