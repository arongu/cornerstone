package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.common.logmsg.CommonLogMessages;
import cornerstone.webapp.datasources.AccountsDB;
import cornerstone.webapp.services.accounts.management.exceptions.account.common.ParameterNotSetException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountNotExistsException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountRetrievalException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class AccountManagerImpl implements AccountManager {
    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    private static final String SQL_ACCOUNT_CREATE                  = "INSERT INTO accounts.accounts (account_id, email_address, password_hash) VALUES (?,?,?)";
    private static final String SQL_ACCOUNT_SELECT_BY_EMAIL_ADDRESS = "SELECT * FROM accounts.accounts WHERE email_address=?";
    private static final String SQL_CREATE_SUB_ACCOUNT   = "INSERT INTO accounts.accounts (acaccount_group_id, account_id, email_address, password_hash) VALUES (?,?,?,?)";
    private static final String SQL_CREATE_ACCOUNT_GROUP = "INSERT INTO accounts.account_groups (account_group_id, account_group_owner_id, account_group_name, account_group_notes) VALUES (?,?,?,?)";

    // Log messages
    private static final String LOG_INF_ACCOUNT_CREATION   = "Account created '%s'";
    private static final String LOG_ERR_ACCOUNT_CREATION   = "Failed to create account '%s', message: '%s', SQL state: '%s'";
    private static final String LOG_INF_ACCOUNT_SELECTION  = "Account retrieved '%s'";
    private static final String LOG_INF_ACCOUNT_NOT_EXISTS = "Account does not exist '%s'";
    private static final String LOG_ERR_ACCOUNT_SELECTION  = "Failed to select account '%s', message: '%s', SQL state: '%s'";

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

        try (final Connection conn = accountsDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_ACCOUNT_CREATE)) {
            ps.setObject(1, accountId);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            final int updates = ps.executeUpdate();
            logger.info(String.format(LOG_INF_ACCOUNT_CREATION, email));
            return updates;

        } catch (final SQLException e) {
            final String errorLog = String.format(LOG_ERR_ACCOUNT_CREATION, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new CreationException(email);
        }
    }

    @Override
    public int createSubAccount(UUID groupId, UUID accountId, String email, String passwordHash) throws ParameterNotSetException {
        return 0;
    }

    @Override
    public AccountResultSet get(final String email) throws AccountRetrievalException, AccountNotExistsException, ParameterNotSetException {
        if ( email == null) throw new ParameterNotSetException("email");

        try (final Connection conn = accountsDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_ACCOUNT_SELECT_BY_EMAIL_ADDRESS)) {
            ps.setString(1, email);
            final ResultSet rs = ps.executeQuery();
            if ( rs != null && rs.next()) {
                return new AccountResultSet(
                        rs.getString   ("account_group_id"),
                        rs.getString   ("account_id"),
                        rs.getTimestamp("account_creation_ts"),
                        rs.getBoolean  ("account_locked"),
                        rs.getTimestamp("account_locked_ts"),
                        rs.getString   ("account_lock_reason"),
                        rs.getTimestamp("account_lock_reason_ts"),
                        rs.getInt      ("login_attempts"),
                        rs.getString   ("last_login_attempt_ip"),
                        rs.getTimestamp("last_login_attempt_ip_ts"),
                        rs.getString   ("last_successful_login_ip"),
                        rs.getTimestamp("last_successful_login_ip_ts"),
                        rs.getString   ("email_address"),
                        rs.getTimestamp("email_address_ts"),
                        rs.getBoolean  ("email_address_verified"),
                        rs.getTimestamp("email_address_verified_ts"),
                        rs.getString   ("password_hash"),
                        rs.getTimestamp("password_hash_ts"),
                        rs.getString   ("superpowers"),
                        rs.getTimestamp("superpowers_ts")
                );

            } else {
                logger.info(String.format(LOG_INF_ACCOUNT_NOT_EXISTS, email));
                throw new AccountNotExistsException(email);
            }

        } catch (final SQLException e) {
            final String errorLog = String.format(LOG_ERR_ACCOUNT_SELECTION, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new AccountRetrievalException(email);
        }
    }
}
