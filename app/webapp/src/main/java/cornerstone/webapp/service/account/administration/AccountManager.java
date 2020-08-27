package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountDeletionException;
import cornerstone.webapp.rest.endpoint.account.AccountSetup;
import cornerstone.webapp.service.account.administration.exceptions.*;

import java.util.List;

public interface AccountManager {
    int create(final String email, final String password, final boolean locked, final boolean verified) throws AccountCreationException;

    int create(final List<AccountSetup> list) throws AccountBulkCreationException, AccountBulkCreationInitalException;
    AccountResultSet get(final String email) throws AccountGetException, AccountDoesNotExistException;

    int setPassword(final String email, final String password) throws AccountUpdatePasswordException;
    int setEmail(final String currentEmail, final String newEmail) throws AccountUpdateEmailException;

    int incrementLoginAttempts(final String email) throws AccountUpdateLoginAttemptsException;
    int clearLoginAttempts(final String email) throws AccountUpdateLoginAttemptsException;

    int lock(final String email, final String reason) throws AccountUpdateLockException;
    int unlock(final String email) throws AccountUpdateLockException;


    int delete(final String email) throws AccountDeletionException;
    int delete(final List<String> emails) throws AccountBulkDeletionException, AccountBulkDeletionInitialException;

    boolean login(final String email, final String password) throws AccountLockedException, AccountEmailNotVerifiedException, AccountDoesNotExistException;
}
