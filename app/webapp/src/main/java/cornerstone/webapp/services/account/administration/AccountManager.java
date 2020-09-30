package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.services.account.administration.exceptions.single.DeletionException;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSetup;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.account.administration.exceptions.single.*;

import java.util.List;

public interface AccountManager {
    int create(final String email, final String password, final boolean locked, final boolean verified) throws CreationException;
    int create(final List<AccountSetup> list) throws MultiCreationException, MultiCreationInitialException;

    AccountResultSet get(final String email) throws RetrievalException, NoAccountException;
    List<String> searchAccounts(final String str) throws EmailAddressSearchException;

    int setPassword(final String email, final String password) throws PasswordUpdateException;
    int setEmail(final String currentEmail, final String newEmail) throws EmailUpdateException;

    int incrementLoginAttempts(final String email) throws LoginAttemptsUpdateException;
    int clearLoginAttempts(final String email) throws LoginAttemptsUpdateException;

    int lock(final String email, final String reason) throws LockUpdateException;
    int unlock(final String email) throws LockUpdateException;

    int delete(final String email) throws DeletionException, NoAccountException;
    int delete(final List<String> emails) throws MultiDeletionException, MultiDeletionInitialException;

    boolean login(final String email, final String password) throws LockedException, UnverifiedEmailException, NoAccountException;
}
