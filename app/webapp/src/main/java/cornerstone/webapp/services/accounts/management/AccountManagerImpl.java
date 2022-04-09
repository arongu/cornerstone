package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.common.logmsg.CommonLogMessages;
import cornerstone.webapp.datasources.AccountsDB;
import cornerstone.webapp.services.accounts.management.exceptions.account.common.ParameterNotSetException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;


public class AccountManagerImpl implements AccountManager {
    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    private static final String SQL_CREATE_ACCOUNT       = "INSERT INTO accounts.accounts (account_id, email_address, password_hash) VALUES (?,?,?)";
    private static final String SQL_CREATE_SUB_ACCOUNT   = "INSERT INTO accounts.accounts (acaccount_group_id, account_id, email_address, password_hash) VALUES (?,?,?,?)";
    private static final String SQL_CREATE_ACCOUNT_GROUP = "INSERT INTO accounts.account_groups (account_group_id, account_group_owner_id, account_group_name, account_group_notes) VALUES (?,?,?,?)";

    // Log messages
    private static final String INFO_LOG_ACCOUNT_LOG_CREATION = "Account created '%s'";
    private static final String ERROR_LOG_ACCOUNT_CREATION_FAILED = "Failed to create account '%s', message: '%s', SQL state: '%s'";

    private final AccountsDB accountsDB;

    @Inject
    public AccountManagerImpl(final AccountsDB accountsDB) {
        this.accountsDB = accountsDB;
        final String logMsg = String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName());
        logger.info(logMsg);
    }

    @Override
    public int createGroup(final UUID groupId, final UUID groupOwnerId, final String groupName, final String groupNotes, int currentMembers, int maxMembers) throws ParameterNotSetException {
        return 0;
    }

    @Override
    public int createAccount(final UUID accountId, final String email, final String passwordHash) throws ParameterNotSetException, CreationException {
        if ( accountId == null)     throw new ParameterNotSetException("accountId");
        if ( email == null)         throw new ParameterNotSetException("email");
        if ( passwordHash == null)  throw new ParameterNotSetException("passwordHash");

        try (final Connection conn = accountsDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
            ps.setObject(1, accountId);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            final int updates = ps.executeUpdate();
            logger.info(String.format(INFO_LOG_ACCOUNT_LOG_CREATION, email));
            return updates;

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new CreationException(email);
        }
    }

    @Override
    public int createSubAccount(UUID groupId, UUID accountId, String email, String passwordHash) throws ParameterNotSetException {
        return 0;
    }
}
