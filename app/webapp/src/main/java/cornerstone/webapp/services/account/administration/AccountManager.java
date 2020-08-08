package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;

import java.util.List;
import java.util.NoSuchElementException;

public interface AccountManager {
    // Create
    int create(final String emailAddress, final String password, final boolean accountLocked, final boolean verified) throws AccountManagerSqlException;
    int create(final List<AccountEmailPassword> list) throws AccountManagerSqlBulkException, AccountManagerSqlException;

    // Read
    AccountResultSet get(final String emailAddress) throws AccountManagerSqlException, NoSuchElementException;

    // Update
    int setPassword(final String emailAddress, final String password) throws AccountManagerSqlException;
    int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountManagerSqlException;

    int incrementLoginAttempts(final String emailAddress) throws AccountManagerSqlException;
    int clearLoginAttempts(final String emailAddress) throws AccountManagerSqlException;

    int lock(final String emailAddress, final String reason) throws AccountManagerSqlException;
    int unlock(final String emailAddress) throws AccountManagerSqlException;

    // Delete
    int delete(final String emailAddress) throws AccountManagerSqlException;
    int delete(final List<String> emailAddresses) throws AccountManagerSqlException, AccountManagerSqlBulkException;

    // Login
    boolean login(final String emailAddress, final String password) throws AccountManagerSqlException;
}
