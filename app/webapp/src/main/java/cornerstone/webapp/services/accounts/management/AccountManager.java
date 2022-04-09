package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.services.accounts.management.exceptions.account.common.ParameterNotSetException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountNotExistsException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountRetrievalException;

import java.util.UUID;

public interface AccountManager {
    // TODO add accountId based delete update etc
    int createGroup(final UUID groupId, final UUID groupOwnerId, final String groupName, final String groupNotes, final int currentMembers, final int maxMembers) throws ParameterNotSetException;

    // createSuperAccount
    int createAccount                       (final UUID accountId, final String email, final String passwordHash) throws ParameterNotSetException, CreationException;
    int createSubAccount(final UUID groupId, final UUID accountId, final String email, final String passwordHash) throws ParameterNotSetException;
    //    int createSuper(final UUID groupId, final UUID accountId, final String email, final String passwordHash, final boolean emailVerified);
//    int create(final List<AccountSetup> list) throws MultiCreationException, MultiCreationInitialException;

      AccountResultSet get(final String email) throws AccountRetrievalException, AccountNotExistsException, ParameterNotSetException;
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
//      int delete(final String email)        throws DeletionException, NoAccountException;
//      int delete(final UUID account_id)     throws DeletionException, NoAccountException;
//    int delete(final List<String> emails) throws MultiDeletionException, MultiDeletionInitialException;
//
//    AccountResultSet login(final String email, final String password) throws LockedException, UnverifiedEmailException, NoAccountException, RetrievalException, BadPasswordException;
}
