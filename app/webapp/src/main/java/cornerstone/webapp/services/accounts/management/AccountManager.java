package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.services.accounts.management.enums.ACCOUNT_TYPE_ENUM;
import cornerstone.webapp.services.accounts.management.enums.MULTI_ACCOUNT_ROLE_ENUM;
import cornerstone.webapp.services.accounts.management.enums.SYSTEM_ROLE_ENUM;
import cornerstone.webapp.services.accounts.management.exceptions.single.DeletionException;
import cornerstone.webapp.rest.api.accounts.dtos.AccountSetup;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.single.*;

import java.util.List;
import java.util.UUID;

public interface AccountManager {
    // TODO add accountId based delete update etc
    int create(final SYSTEM_ROLE_ENUM        systemRole,
               final UUID                    accountId,
               final ACCOUNT_TYPE_ENUM       accountType,
               final String                  email,
               final String                  password,
               final boolean                 locked,
               final String                  lockReason,
               final boolean                 verified,
               final MULTI_ACCOUNT_ROLE_ENUM multiAccountRole,
               final UUID                    parentAccountId) throws CreationException, CreationDuplicateException, CreationNullException;

//    int create(final List<AccountSetup> list) throws MultiCreationException, MultiCreationInitialException;

//    AccountResultSet get(final String email)          throws RetrievalException, NoAccountException;
//    AccountResultSet get(final UUID account_id)       throws RetrievalException, NoAccountException;
//
//    List<String> searchAccounts(final String keyword) throws AccountSearchException;
//
//    int setPassword(final String email,        final String password)      throws PasswordUpdateException;
//    int setEmail   (final String currentEmail, final String newEmail)      throws EmailUpdateException;
//    int setRole    (final String email,        final SYSTEM_ROLE_ENUM accountRole) throws RoleUpdateException;
//
//    int incrementLoginAttempts(final String email) throws LoginAttemptsUpdateException;
//    int clearLoginAttempts    (final String email) throws LoginAttemptsUpdateException;
//
//    int lock  (final String email, final String reason) throws LockUpdateException;
//    int unlock(final String email)                      throws LockUpdateException;
//
      int delete(final String email)        throws DeletionException, NoAccountException;
      int delete(final UUID account_id)     throws DeletionException, NoAccountException;
//    int delete(final List<String> emails) throws MultiDeletionException, MultiDeletionInitialException;
//
//    AccountResultSet login(final String email, final String password) throws LockedException, UnverifiedEmailException, NoAccountException, RetrievalException, BadPasswordException;
}
