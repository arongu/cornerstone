package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.DeletionException;
import cornerstone.webapp.rest.endpoint.account.AccountSetup;
import cornerstone.webapp.service.account.administration.exceptions.bulk.PartialCreationException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkCreationException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.PartialDeletionException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkDeleteException;
import cornerstone.webapp.service.account.administration.exceptions.single.*;

import java.util.List;

public interface AccountManager {
    int create(final String email, final String password, final boolean locked, final boolean verified) throws CreationException;

    int create(final List<AccountSetup> list) throws PartialCreationException, BulkCreationException;
    AccountResultSet get(final String email) throws RetrievalException, NoAccountException;

    int setPassword(final String email, final String password) throws PasswordUpdateException;
    int setEmail(final String currentEmail, final String newEmail) throws EmailUpdateException;

    int incrementLoginAttempts(final String email) throws LoginAttemptsUpdateException;
    int clearLoginAttempts(final String email) throws LoginAttemptsUpdateException;

    int lock(final String email, final String reason) throws LockUpdateException;
    int unlock(final String email) throws LockUpdateException;

    int delete(final String email) throws DeletionException, NoAccountException;
    int delete(final List<String> emails) throws PartialDeletionException, BulkDeleteException;

    boolean login(final String email, final String password) throws LockedException, UnverifiedEmailException, NoAccountException;
}
