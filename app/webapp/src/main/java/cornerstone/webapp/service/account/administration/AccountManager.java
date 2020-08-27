package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.AccountDeletionException;
import cornerstone.webapp.rest.endpoint.account.AccountSetup;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkCreationException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkCreationInitialException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkDeletionException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkDeletionInitialException;
import cornerstone.webapp.service.account.administration.exceptions.single.*;

import java.util.List;

public interface AccountManager {
    int create(final String email, final String password, final boolean locked, final boolean verified) throws AccountCreationException;

    int create(final List<AccountSetup> list) throws BulkCreationException, BulkCreationInitialException;
    AccountResultSet get(final String email) throws AccountRetrievalException, AccountDoesNotExistException;

    int setPassword(final String email, final String password) throws UpdatePasswordException;
    int setEmail(final String currentEmail, final String newEmail) throws UpdateEmailException;

    int incrementLoginAttempts(final String email) throws UpdateLoginAttemptsException;
    int clearLoginAttempts(final String email) throws UpdateLoginAttemptsException;

    int lock(final String email, final String reason) throws UpdateLockException;
    int unlock(final String email) throws UpdateLockException;


    int delete(final String email) throws AccountDeletionException, AccountDoesNotExistException;
    int delete(final List<String> emails) throws BulkDeletionException, BulkDeletionInitialException;

    boolean login(final String email, final String password) throws AccountLockedException, AccountEmailNotVerifiedException, AccountDoesNotExistException;
}
