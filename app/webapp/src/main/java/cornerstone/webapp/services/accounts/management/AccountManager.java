package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.services.accounts.management.exceptions.single.DeletionException;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSetup;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.single.*;

import java.util.List;

public interface AccountManager {
    int create(final String email, final String password,
               final boolean locked, final boolean verified,
               final AccountRole accountRole) throws CreationException, CreationDuplicateException, CreationNullException;

    int create(final List<AccountSetup> list) throws MultiCreationException, MultiCreationInitialException;

    AccountResultSet get(final String email) throws RetrievalException, NoAccountException;
    List<String> searchAccounts(final String keyword) throws AccountSearchException;

    int setPassword(final String email, final String password) throws PasswordUpdateException;
    int setEmail(final String currentEmail, final String newEmail) throws EmailUpdateException;
    int setRole(final String email, final AccountRole accountRole) throws RoleUpdateException;

    int incrementLoginAttempts(final String email) throws LoginAttemptsUpdateException;
    int clearLoginAttempts(final String email) throws LoginAttemptsUpdateException;

    int lock(final String email, final String reason) throws LockUpdateException;
    int unlock(final String email) throws LockUpdateException;

    int delete(final String email) throws DeletionException, NoAccountException;
    int delete(final List<String> emails) throws MultiDeletionException, MultiDeletionInitialException;

    AccountResultSet login(final String email, final String password) throws LockedException, UnverifiedEmailException, NoAccountException, RetrievalException, BadPasswordException;
}
