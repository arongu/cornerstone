package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationDuplicateException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationNullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class AccountManagerImpl implements AccountManager {
    private static final String CREATED                    = "CREATED '%s'";
    private static final String DELETED                    = "DELETED '%s'";
    private static final String DELETE_NO_ACCOUNT          = "NO ACCOUNT TO DELETE '%s'";
    private static final String EMAIL_ADDRESS_CHANGED      = "EMAIL ADDRESS CHANGED '%s' -> '%s'";
    private static final String LOCKED                     = "LOCKED '%s'";
    private static final String LOGGED_IN                  = "LOGGED IN '%s'";
    private static final String LOGIN_ATTEMPTS_CLEARED     = "LOGIN ATTEMPTS CLEARED '%s'";
    private static final String LOGIN_ATTEMPTS_INCREMENTED = "LOGIN ATTEMPTS INCREMENTED '%s'";
    private static final String LOGIN_FAILED               = "LOGIN FAILED '%s'";
    private static final String PASSWORD_CHANGED           = "PASSWORD CHANGED '%s'";
    private static final String RETRIEVE_NO_ACCOUNT        = "NO ACCOUNT TO RETRIEVE '%s'";
    private static final String RETRIEVED                  = "RETRIEVED '%s'";
    private static final String ROLE_CHANGED               = "ROLE CHANGED '%s'";
    private static final String UNLOCKED                   = "UNLOCKED '%s'";

    // SQL group statements
    private static final String SQL_CREATE_GROUP                 = "INSERT INTO users.groups (group_id, group_owner_id, group_name, group_notes, max_users) VALUES(?,?,?,?,?)";
    private static final String SQL_CREATE_ACCOUNT               = "INSERT INTO users.accounts (account_id, email_address, password_hash) VALUES (?,?,?)";
    private static final String SQL_CREATE_ACCOUNT_WITH_GROUP_ID = "INSERT INTO users.accounts (group_id, account_id, email_address, password_hash) VALUES (?,?,?,?)";

    // SQL statements
    private static final String SQL_SELECT_ACCOUNT_BY_EMAIL                       = "SELECT roles.name AS role_name, accounts.*, account_http_method_permissions.delete, account_http_method_permissions.get, account_http_method_permissions.head, account_http_method_permissions.options, account_http_method_permissions.patch, account_http_method_permissions.post, account_http_method_permissions.put FROM users.accounts LEFT JOIN system.roles ON (accounts.role_id = roles.id) LEFT JOIN users.account_http_method_permissions ON (accounts.account_id = account_http_method_permissions.account_id) WHERE email_address=(?)";
    private static final String SQL_SELECT_ACCOUNT_BY_ID                          = "SELECT roles.name AS role_name, accounts.*, account_http_method_permissions.delete, account_http_method_permissions.get, account_http_method_permissions.head, account_http_method_permissions.options, account_http_method_permissions.patch, account_http_method_permissions.post, account_http_method_permissions.put FROM users.accounts LEFT JOIN system.roles ON (accounts.role_id = roles.id) LEFT JOIN users.account_http_method_permissions ON (accounts.account_id = account_http_method_permissions.account_id) WHERE account_id=(?)";
    private static final String SQL_SELECT_ACCOUNTS_ILIKE                         = "SELECT email_address FROM users.accounts WHERE email_address ILIKE (?)";
    private static final String SQL_INSERT_ACCOUNT                                = "INSERT INTO users.accounts (system_role_id, account_id, account_type_id, account_locked, account_lock_reason, email_address, email_address_verified, password_hash, multi_account_role_id, parent_account_id) VALUES(?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT_BY_EMAIL_ADDRESS               = "DELETE FROM users.accounts WHERE email_address=(?)";
    private static final String SQL_DELETE_ACCOUNT_BY_ACCOUNT_ID                  = "DELETE FROM users.accounts WHERE account_id=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD                       = "UPDATE users.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS                  = "UPDATE users.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED                         = "UPDATE users.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_ROLE                           = "UPDATE users.accounts SET role_id=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT               = "UPDATE users.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR                   = "UPDATE users.accounts SET account_login_attempts=0 WHERE email_address=(?)";

    // Log messages
    private static final String ERROR_LOG_ACCOUNT_GET_FAILED                      = "Failed to get '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_LOG_ACCOUNT_SEARCH_RUN_FAILED               = "Failed to search through account email addresses with keyword: '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_LOG_ACCOUNT_CREATION_FAILED                 = "Failed to create '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_ACCOUNT_DELETION_FAILED                 = "Failed to delete '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_PASSWORD_FAILED                  = "Failed to update password of '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_EMAIL_FAILED                     = "Failed to update email addr. of '%s' -> '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_LOCK_FAILED                      = "Failed to update lock of '%s', locked: '%s', account_lock_reason: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_ROLE_FAILED                      = "Failed to update account role of '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_INCREMENT_LOGIN_ATTEMPTS_FAILED         = "Failed to increment login attempts of '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_CLEAR_LOGIN_ATTEMPTS_FAILED             = "Failed to clear login attempts of '%s', message: '%s', SQL state: '%s'";

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    private static int executeSqlUpdate(final String email,
                                        final String sqlStatement,
                                        final UsersDB usersDB) throws SQLException {

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
            ps.setString(1, email);
            return ps.executeUpdate();
        }
    }

    private final UsersDB usersDB;
    private final ConfigLoader configLoader;

    @Inject
    public AccountManagerImpl(final UsersDB usersDB, final ConfigLoader configLoader) {
        this.usersDB = usersDB;
        this.configLoader = configLoader;

        final String logMsg = String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName());
        logger.info(logMsg);
    }

    @Override
    public int createGroup(final UUID groupId, final UUID ownerId, final String groupName, final String groupNotes, final int maxUsers) throws CreationNullException {
        if (groupId == null || ownerId == null || groupName == null || groupName.isEmpty() || groupNotes == null || groupNotes.isEmpty()) {
            throw new CreationNullException();
        }

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_GROUP)) {
            ps.setObject (1, groupId);
            ps.setObject (2, ownerId);
            ps.setString (3, groupName);
            ps.setString (4, groupNotes);
            ps.setInt    (5, maxUsers);

            final int updates = ps.executeUpdate();
//            final String logMsg = String.format(CREATED, groupId);

//            logger.info(logMsg);
            return updates;

        } catch (final SQLException e) {
//            final String errorLog = String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, email, e.getMessage(), e.getSQLState());
//            logger.error(errorLog);
                logger.error(e.getMessage());

            if ( e.getSQLState().equals("23505")) {
//                throw new CreationDuplicateException();
                throw new CreationNullException();
            } else {
//                throw new CreationException(email);
                throw new CreationNullException();
            }
        }
    }

    @Override
    public int createAccount(final UUID accountId, final String email, final String passwordHash) throws CreationNullException {
        if ( accountId == null || email == null || email.isEmpty() || passwordHash == null || passwordHash.isEmpty()){
            throw new CreationNullException("asd");
        }

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
            ps.setObject(1, accountId);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            final int updates = ps.executeUpdate();
            return updates;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    @Override
    public int createAccountAndAddToGroup(final UUID groupId, final UUID accountId, final String email, final String passwordHash) throws CreationNullException {
        if ( groupId == null || accountId == null || email == null || email.isEmpty() || passwordHash == null || passwordHash.isEmpty()){
            throw new CreationNullException("asd");
        }

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT_WITH_GROUP_ID)) {
            ps.setObject(1, groupId);
            ps.setObject(2, accountId);
            ps.setString(3, email);
            ps.setString(4, passwordHash);

            final int updates = ps.executeUpdate();
            return updates;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }
}
