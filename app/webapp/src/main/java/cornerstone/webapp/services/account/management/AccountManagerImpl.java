package cornerstone.webapp.services.account.management;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.config.enums.APP_ENUM;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSetup;
import cornerstone.webapp.services.account.management.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.account.management.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.account.management.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.account.management.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.account.management.exceptions.single.*;
import org.apache.commons.codec.digest.Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AccountManagerImpl implements AccountManager {
    private static final String CREATED                    = "CREATED";
    private static final String DELETED                    = "DELETED";
    private static final String DELETE_NO_ACCOUNT          = "NO ACCOUNT TO DELETE";
    private static final String EMAIL_ADDRESS_CHANGED      = "EMAIL ADDRESS CHANGED";
    private static final String LOCKED                     = "LOCKED";
    private static final String LOGGED_IN                  = "LOGGED IN";
    private static final String LOGIN_ATTEMPTS_CLEARED     = "LOGIN ATTEMPTS CLEARED";
    private static final String LOGIN_ATTEMPTS_INCREMENTED = "LOGIN ATTEMPTS INCREMENTED";
    private static final String LOGIN_FAILED               = "LOGIN FAILED";
    private static final String PASSWORD_CHANGED           = "PASSWORD CHANGED";
    private static final String RETRIEVED                  = "RETRIEVED";
    private static final String UNLOCKED                   = "UNLOCKED";

    // SQL statements
    private static final String SQL_SELECT_ACCOUNT                                = "SELECT accounts.*, roles.role_name FROM user_data.accounts JOIN user_data.roles ON (accounts.role_id = roles.role_id) WHERE email_address=(?)";
    private static final String SQL_SELECT_ACCOUNTS_ILIKE                         = "SELECT email_address FROM user_data.accounts WHERE email_address ILIKE (?)";
    private static final String SQL_SELECT_ACCOUNT_FOR_LOGIN                      = "SELECT email_address, email_address_verified, account_locked, account_login_attempts, password_hash, accounts.role_id, role_name FROM user_data.accounts JOIN user_data.roles ON (accounts.role_id = roles.role_id) WHERE email_address=(?)";
    private static final String SQL_INSERT_ACCOUNT                                = "INSERT INTO user_data.accounts (password_hash, email_address, account_locked, email_address_verified, role_id) VALUES(?,?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                                = "DELETE FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD                       = "UPDATE user_data.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS                  = "UPDATE user_data.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED                         = "UPDATE user_data.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT               = "UPDATE user_data.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR                   = "UPDATE user_data.accounts SET account_login_attempts=0 WHERE email_address=(?)";

    // Log messages
    private static final String ERROR_LOG_ACCOUNT_GET_FAILED                      = "Failed to get '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_LOG_ACCOUNT_SEARCH_RUN_FAILED               = "Failed to search through account email addresses with keyword: '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_LOG_ACCOUNT_CREATION_FAILED                 = "Failed to create '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_ACCOUNT_DELETION_FAILED                 = "Failed to delete '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_PASSWORD_FAILED                  = "Failed to update password of '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_EMAIL_FAILED                     = "Failed to update email addr. of '%s' -> '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_LOCK_FAILED                      = "Failed to update lock of '%s', locked: '%s', account_lock_reason: '%s', message: '%s', SQL state: '%s'";
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
    public AccountResultSet get(final String email) throws NoAccountException, RetrievalException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACCOUNT)) {
            ps.setString(1, email.toLowerCase());
            final ResultSet rs = ps.executeQuery();

            if ( rs.next()) {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        RETRIEVED, email
                );

                logger.info(logMsg);
                return new AccountResultSet(
                        rs.getInt      ("account_id"),
                        rs.getTimestamp("account_registration_ts"),
                        rs.getBoolean  ("account_locked"),
                        rs.getTimestamp("account_locked_ts"),
                        rs.getString   ("account_lock_reason"),
                        rs.getInt      ("account_login_attempts"),
                        rs.getString   ("email_address"),
                        rs.getTimestamp("email_address_ts"),
                        rs.getBoolean  ("email_address_verified"),
                        rs.getTimestamp("email_address_verified_ts"),
                        rs.getString   ("password_hash"),
                        rs.getTimestamp("password_hash_ts"),
                        rs.getInt      ("role_id"),
                        rs.getString   ("role_name")
                );

            } else {
                throw new NoAccountException(email);
            }

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_ACCOUNT_GET_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new RetrievalException(email);
        }
    }

    @Override
    public List<String> searchAccounts(final String keyword) throws AccountSearchException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACCOUNTS_ILIKE)) {
            ps.setString(1, keyword);
            final ResultSet rs = ps.executeQuery();

            final LinkedList<String> results = new LinkedList<>();
            while (rs.next()) {
                results.add(rs.getString("email_address"));
            }

            return results;

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_ACCOUNT_SEARCH_RUN_FAILED, keyword, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new AccountSearchException(keyword);
        }
    }

    @Override
    public int create(final String email, final String password, final boolean locked, final boolean verified, final AccountRole accountRole)
            throws CreationException, CreationDuplicateException, CreationNullException {

        if (email == null || password == null || accountRole == null) {
            throw new CreationNullException();
        }

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ACCOUNT)) {
            ps.setString (1, Crypt.crypt(password));
            ps.setString (2, email.toLowerCase());
            ps.setBoolean(3, locked);
            ps.setBoolean(4, verified);
            ps.setInt(5, accountRole.getId());

            final int updates = ps.executeUpdate();
            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR, AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    CREATED, email
            );

            logger.info(logMsg);
            return updates;

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);

            if ( e.getSQLState().equals("23505")) {
                throw new CreationDuplicateException(email);
            } else {
                throw new CreationException(email);
            }
        }
    }

    @Override
    public int create(final List<AccountSetup> accountSetupList) throws MultiCreationException, MultiCreationInitialException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ACCOUNT)) {
            MultiCreationException multiCreationException = null;
            int updatedRows = 0;

            for (final AccountSetup accountSetup : accountSetupList) {
                try {
                    if ( accountSetup == null) {
                        throw new CreationNullException();
                    }

                    if ( accountSetup.getRole() == null || accountSetup.getEmail() == null || accountSetup.getPassword() == null) {
                        throw new CreationNullException();
                    }

                    final String email     = accountSetup.getEmail();
                    final String password  = accountSetup.getPassword();
                    final String role      = accountSetup.getRole();
                    final boolean locked   = accountSetup.isLocked();
                    final boolean verified = accountSetup.isVerified();
                    int role_id;

                    try {
                        role_id = AccountRole.valueOf(role).getId();

                    } catch (final IllegalArgumentException e) {
                        role_id = AccountRole.NO_ROLE.getId();
                    }

                    ps.setString (1, Crypt.crypt(password));
                    ps.setString (2, email.toLowerCase());
                    ps.setBoolean(3, locked);
                    ps.setBoolean(4, verified);
                    ps.setInt(5, role_id);

                    updatedRows += ps.executeUpdate();
                    final String logMsg = String.format(
                            AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                            AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                            CREATED, email
                    );
                    logger.info(logMsg);

                } catch (final CreationNullException e) {
                    if ( multiCreationException == null) {
                        multiCreationException = new MultiCreationException();
                    }

                    multiCreationException.addExceptionMessage(e.getMessage());

                } catch (final SQLException e) {
                    if ( multiCreationException == null) {
                        multiCreationException = new MultiCreationException();
                    }

                    final String exceptionMsg;
                    if ( e.getSQLState().equals("23505")) {
                        exceptionMsg = String.format(CreationDuplicateException.EXCEPTION_MESSAGE_ACCOUNT_CREATION_ALREADY_EXISTS, accountSetup.getEmail());
                    } else {
                        exceptionMsg = String.format(CreationException.EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, accountSetup.getEmail());
                    }

                    multiCreationException.addExceptionMessage(exceptionMsg);
                    final String errorLog = String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, accountSetup.getEmail(), e.getMessage(), e.getSQLState());
                    logger.error(errorLog);
                }
            }

            if ( multiCreationException != null) {
                throw multiCreationException;
            } else {
                return updatedRows;
            }

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            throw new MultiCreationInitialException();
        }
    }

    @Override
    public int delete(final String email) throws DeletionException, NoAccountException {
        try {
            final int deletedRows = executeSqlUpdate(email, SQL_DELETE_ACCOUNT, usersDB);
            if ( deletedRows > 0) {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        DELETED, email
                );

                logger.info(logMsg);
                return deletedRows;

            } else {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        DELETE_NO_ACCOUNT, email
                );

                logger.info(logMsg);
                throw new NoAccountException(email);
            }

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_ACCOUNT_DELETION_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new DeletionException(email);
        }
    }

    @Override
    public int delete(final List<String> emails) throws MultiDeletionException, MultiDeletionInitialException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
            MultiDeletionException multiDeletionException = null;
            int deletedRows = 0;

            for (final String email : emails) {
                try {
                    ps.setString(1, email);
                    final int n = ps.executeUpdate();

                    if ( n > 0) {
                        deletedRows += n;
                        final String logMsg = String.format(
                                AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                                DELETED, email
                        );

                        logger.info(logMsg);

                    } else {
                        final String logMsg = String.format(
                                AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                                DELETE_NO_ACCOUNT, email
                        );

                        logger.info(logMsg);
                        throw new NoAccountException(email);
                    }

                } catch (final NoAccountException n) {
                    if ( multiDeletionException == null) {
                        multiDeletionException = new MultiDeletionException();
                    }

                    multiDeletionException.addExceptionMessage(n.getMessage());

                } catch (final SQLException s) {
                    if ( multiDeletionException == null) {
                        multiDeletionException = new MultiDeletionException();
                    }

                    final String exceptionMsg = String.format(DeletionException.EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, email);
                    final String errorLog     = String.format(ERROR_LOG_ACCOUNT_DELETION_FAILED, email, s.getMessage(), s.getSQLState());

                    multiDeletionException.addExceptionMessage(exceptionMsg);
                    logger.error(errorLog);
                }
            }

            if ( null != multiDeletionException) {
                throw multiDeletionException;
            } else {
                return deletedRows;
            }

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            throw new MultiDeletionInitialException();
        }
    }

    @Override
    public int setPassword(final String email, final String password) throws PasswordUpdateException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)) {
            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, email);
            final int updatedRows = ps.executeUpdate();

            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    PASSWORD_CHANGED, email
            );

            logger.info(logMsg);
            return updatedRows;

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_UPDATE_PASSWORD_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new PasswordUpdateException(email);
        }
    }

    @Override
    public int setEmail(final String currentEmail, final String newEmail) throws EmailUpdateException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {
            ps.setString(1, newEmail);
            ps.setString(2, currentEmail);
            final int updatedRows = ps.executeUpdate();

            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    EMAIL_ADDRESS_CHANGED, "'" + currentEmail + "' -> '" + newEmail + "'"
            );

            logger.info(logMsg);
            return updatedRows;

        } catch (final SQLException e) {
            final String errorLog     = String.format(ERROR_LOG_UPDATE_EMAIL_FAILED, currentEmail, newEmail, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new EmailUpdateException(currentEmail, newEmail);
        }
    }

    @Override
    public int lock(final String email, final String reason) throws LockUpdateException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {
            ps.setBoolean(1,true);
            ps.setString(2, reason);
            ps.setString(3, email);
            final int updatedRows = ps.executeUpdate();

            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    LOCKED, email
            );

            logger.info(logMsg);
            return updatedRows;

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_UPDATE_LOCK_FAILED, email, true, reason, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new LockUpdateException(email);
        }
    }

    @Override
    public int unlock(final String email) throws LockUpdateException {
        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {
            ps.setBoolean(1,false);
            ps.setString(2, null);
            ps.setString(3, email);
            final int updatedRows = ps.executeUpdate();

            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    UNLOCKED, email
            );

            logger.info(logMsg);
            return updatedRows;

        } catch (final SQLException e) {
            final String errorLog     = String.format(ERROR_LOG_UPDATE_LOCK_FAILED, email, false, "???", e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new LockUpdateException(email);
        }
    }

    @Override
    public int incrementLoginAttempts(final String email) throws LoginAttemptsUpdateException {
        try {
            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    LOGIN_ATTEMPTS_INCREMENTED, email
            );

            logger.info(logMsg);
            return executeSqlUpdate(email, SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT, usersDB);

        } catch (final SQLException e) {
            final String errorLog     = String.format(ERROR_LOG_INCREMENT_LOGIN_ATTEMPTS_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new LoginAttemptsUpdateException(email);
        }
    }

    @Override
    public int clearLoginAttempts(final String email) throws LoginAttemptsUpdateException {
        try {
            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    LOGIN_ATTEMPTS_CLEARED, email
            );

            logger.info(logMsg);
            return executeSqlUpdate(email, SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR, usersDB);

        } catch (final SQLException e) {
            final String errorLog = String.format(ERROR_LOG_CLEAR_LOGIN_ATTEMPTS_FAILED, email, e.getMessage(), e.getSQLState());
            logger.error(errorLog);
            throw new LoginAttemptsUpdateException(email);
        }
    }

    @Override
    public boolean login(final String email, final String password) throws
            NoAccountException,
            LockedException,
            UnverifiedEmailException {

        try (final Connection conn = usersDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACCOUNT_FOR_LOGIN)) {
            ps.setString(1, email.toLowerCase());
            final ResultSet rs = ps.executeQuery();
            if ( rs.next() ) {
                final boolean locked      = rs.getBoolean("account_locked");
                final boolean verified    = rs.getBoolean("email_address_verified");
                final int loginAttempts   = rs.getInt    ("account_login_attempts");
                final String passwordHash = rs.getString ("password_hash");

                if ( locked ) {
                    throw new LockedException(email);
                }

                if ( !verified ) {
                    throw new UnverifiedEmailException(email);
                }

                if ( passwordHash.equals(Crypt.crypt(password, passwordHash))) {
                    // clear login attempts on login if needed
                    if ( loginAttempts > 0) {
                        try { clearLoginAttempts(email); }
                        catch (final LoginAttemptsUpdateException ignored) {}
                    }

                    final String logMsg = String.format(AlignedLogMessages.FORMAT__OFFSET_35C_C_STR, AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()), LOGGED_IN, email);
                    logger.info(logMsg);
                    return true;

                } else {
                    final String logMsg = String.format(AlignedLogMessages.FORMAT__OFFSET_35C_C_STR, AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()), LOGIN_FAILED, email);
                    logger.info(logMsg);

                    final int maxLoginAttempts = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key));
                    if ( loginAttempts < maxLoginAttempts ) {
                        try { incrementLoginAttempts(email); }
                        catch (final LoginAttemptsUpdateException ignored) {}

                    } else {
                        try { lock(email, "Maximum login attempts reached."); }
                        catch (final LockUpdateException ignored) {}
                    }

                    return false;
                }

            } else {
                throw new NoAccountException(email);
            }

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_ACCOUNT_GET_FAILED, email, e.getMessage(), e.getSQLState()));
            return false;
        }
    }
}
