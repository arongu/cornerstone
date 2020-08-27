package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.config.enums.APP_ENUM;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.rest.endpoint.account.AccountDeletionException;
import cornerstone.webapp.rest.endpoint.account.AccountSetup;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkCreationException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkCreationInitialException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkDeletionException;
import cornerstone.webapp.service.account.administration.exceptions.bulk.BulkDeletionInitialException;
import cornerstone.webapp.service.account.administration.exceptions.single.*;
import org.apache.commons.codec.digest.Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AccountManagerImpl implements AccountManager {
    private static final String LOG_FORMAT = "%-35s : %s";
    private static final String CREATED                    = "CREATED";
    private static final String DELETED                    = "DELETED";
    private static final String EMAIL_ADDRESS_CHANGED      = "EMAIL ADDRESS CHANGED";
    private static final String LOCKED                     = "LOCKED";
    private static final String LOGGED_IN                  = "LOGGED IN";
    private static final String LOGIN_ATTEMPTS_CLEARED     = "LOGIN ATTEMPTS CLEARED";
    private static final String LOGIN_ATTEMPTS_INCREMENTED = "LOGIN ATTEMPTS INCREMENTED";
    private static final String LOGIN_FAILED               = "LOGIN FAILED";
    private static final String PASSWORD_CHANGED           = "PASSWORD CHANGED";
    private static final String RETRIEVED                  = "RETRIEVED";
    private static final String UNLOCKED                   = "UNLOCKED";

    private static final String SQL_SELECT_ACCOUNT                               = "SELECT * FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_SELECT_ACCOUNT_FOR_LOGIN                     = "SELECT account_locked, email_address_verified, account_login_attempts, password_hash FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_INSERT_ACCOUNT                               = "INSERT INTO user_data.accounts (password_hash, email_address, account_locked, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                               = "DELETE FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD                      = "UPDATE user_data.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS                 = "UPDATE user_data.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED                        = "UPDATE user_data.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT              = "UPDATE user_data.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR                  = "UPDATE user_data.accounts SET account_login_attempts=0 WHERE email_address=(?)";

    // Log, exception messages.
    private static final String ERROR_LOG_ACCOUNT_GET_FAILED                     = "Failed to get '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_LOG_ACCOUNT_CREATION_FAILED                = "Failed to create '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_ACCOUNT_DELETION_FAILED                = "Failed to delete '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_PASSWORD_FAILED                 = "Failed to update password of '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_EMAIL_FAILED                    = "Failed to update email addr. of '%s' -> '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_UPDATE_LOCK_FAILED                     = "Failed to update lock of '%s', locked: '%s', account_lock_reason: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_INCREMENT_LOGIN_ATTEMPTS_FAILED        = "Failed to increment login attempts of '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_LOG_CLEAR_LOGIN_ATTEMPTS_FAILED            = "Failed to clear login attempts of '%s', message: '%s', SQL state: '%s'";

    private static final String EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED        = "Failed to create '%s'.";
    private static final String EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED        = "Failed to delete '%s'.";
    private static final String EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST         = "Account '%s' does not exist.";
    private static final String EXCEPTION_MESSAGE_ACCOUNT_GET_FAILED             = "Failed to get '%s'.";
    private static final String EXCEPTION_MESSAGE_UPDATE_EMAIL_FAILED            = "Failed to update email for '%s' -> '%s'.";
    private static final String EXCEPTION_MESSAGE_UPDATE_LOCK_FAILED             = "Failed to update lock for '%s'.";
    private static final String EXCEPTION_MESSAGE_UPDATE_LOGIN_ATTEMPTS_FAILED   = "Failed to update login attempts for '%s'.";
    private static final String EXCEPTION_MESSAGE_UPDATE_PASSWORD_FAILED         = "Failed to update password for '%s'.";

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    private static int executeSqlUpdateOnAccount(final String email,
                                                 final String sqlStatement,
                                                 final UsersDB usersDB) throws SQLException {

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(sqlStatement)) {

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
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public AccountResultSet get(final String email) throws AccountDoesNotExistException, AccountRetrievalException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_SELECT_ACCOUNT)) {

            ps.setString(1, email.toLowerCase());
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info(String.format(LOG_FORMAT, RETRIEVED, email));
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
                        rs.getTimestamp("password_hash_ts")
                );

            } else {
                throw new AccountDoesNotExistException(String.format(EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST, email));
            }

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_ACCOUNT_GET_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new AccountRetrievalException(String.format(EXCEPTION_MESSAGE_ACCOUNT_GET_FAILED, email));
        }
    }

    @Override
    public int create(final String email, final String password,
                      final boolean locked, final boolean verified) throws AccountCreationException {

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_ACCOUNT)) {

            ps.setString (1, Crypt.crypt(password));
            ps.setString (2, email.toLowerCase());
            ps.setBoolean(3, locked);
            ps.setBoolean(4, verified);

            logger.info(String.format(LOG_FORMAT, CREATED, email));
            return ps.executeUpdate();

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new AccountCreationException(String.format(EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, email));
        }
    }

    @Override
    public int create(final List<AccountSetup> accountSetupList) throws BulkCreationException, BulkCreationInitialException {
        try (final Connection c = usersDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_INSERT_ACCOUNT)) {
            BulkCreationException bulkCreationException = null;
            int updatedRows = 0;

            for (final AccountSetup accountSetup : accountSetupList) {
                if (accountSetup != null) {
                    final String email     = accountSetup.getEmail();
                    final String password  = accountSetup.getPassword();
                    final boolean locked   = accountSetup.isLocked();
                    final boolean verified = accountSetup.isVerified();

                    try {
                        ps.setString (1, Crypt.crypt(password));
                        ps.setString (2, email.toLowerCase());
                        ps.setBoolean(3, locked);
                        ps.setBoolean(4, verified);

                        updatedRows += ps.executeUpdate();
                        logger.info(String.format(LOG_FORMAT, CREATED, email));

                    } catch (final SQLException e) {
                        if (null == bulkCreationException) {
                            bulkCreationException = new BulkCreationException();
                        }

                        bulkCreationException.addExceptionMessage(String.format(EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, email));
                        logger.error(String.format(ERROR_LOG_ACCOUNT_CREATION_FAILED, email, e.getMessage(), e.getSQLState()));
                    }
                }
            }

            if (bulkCreationException != null) {
                throw bulkCreationException;
            } else {
                return updatedRows;
            }

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            throw new BulkCreationInitialException();
        }
    }

    @Override
    public int delete(final String email) throws AccountDeletionException, AccountDoesNotExistException {
        try {
            final int deletedRows = executeSqlUpdateOnAccount(email, SQL_DELETE_ACCOUNT, usersDB);
            if (deletedRows > 0) {
                logger.info(String.format(LOG_FORMAT, DELETED, email));
                return deletedRows;
            } else {
                throw new AccountDoesNotExistException(email);
            }

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_ACCOUNT_DELETION_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new AccountDeletionException(String.format(EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, email));
        }
    }

    @Override
    public int delete(final List<String> emails) throws BulkDeletionException, BulkDeletionInitialException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_DELETE_ACCOUNT) ) {

            BulkDeletionException bulkDeletionException = null;
            int deletedRows = 0;

            for (final String email : emails) {
                try {
                    ps.setString(1, email);
                    deletedRows += ps.executeUpdate();
                    logger.info(String.format(LOG_FORMAT, DELETED, email));

                } catch (final SQLException e) {
                    if (bulkDeletionException == null) {
                        bulkDeletionException = new BulkDeletionException();
                    }

                    bulkDeletionException.addExceptionMessage(String.format(EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, email));
                    logger.error(String.format(ERROR_LOG_ACCOUNT_DELETION_FAILED, email, e.getMessage(), e.getSQLState()));
                }
            }

            if (null != bulkDeletionException) {
                throw bulkDeletionException;
            } else {
                return deletedRows;
            }

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            throw new BulkDeletionInitialException();
        }
    }

    @Override
    public int setPassword(final String email, final String password) throws UpdatePasswordException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)) {

            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, email);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, PASSWORD_CHANGED, email));
            return updatedRows;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_UPDATE_PASSWORD_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new UpdatePasswordException(String.format(EXCEPTION_MESSAGE_UPDATE_PASSWORD_FAILED, email));
        }
    }

    @Override
    public int setEmail(final String currentEmail, final String newEmail) throws UpdateEmailException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {

            ps.setString(1, newEmail);
            ps.setString(2, currentEmail);
            final int updatedRows = ps.executeUpdate();
            // Not creating new LOG_FORMAT for this single case
            logger.info(String.format("%-35s : %s -> %s", EMAIL_ADDRESS_CHANGED, currentEmail, newEmail));
            return updatedRows;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_UPDATE_EMAIL_FAILED, currentEmail, newEmail, e.getMessage(), e.getSQLState()));
            throw new UpdateEmailException(String.format(EXCEPTION_MESSAGE_UPDATE_EMAIL_FAILED, currentEmail, newEmail));
        }
    }

    @Override
    public int lock(final String email, final String reason) throws UpdateLockException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {

            ps.setBoolean(1,true);
            ps.setString(2, reason);
            ps.setString(3, email);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, LOCKED, email));
            return updatedRows;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_UPDATE_LOCK_FAILED, email, true, reason, e.getMessage(), e.getSQLState()));
            throw new UpdateLockException(String.format(EXCEPTION_MESSAGE_UPDATE_LOCK_FAILED, email));
        }
    }

    @Override
    public int unlock(final String email) throws UpdateLockException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {

            ps.setBoolean(1,false);
            ps.setString(2, null);
            ps.setString(3, email);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, UNLOCKED, email));
            return updatedRows;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_UPDATE_LOCK_FAILED, email, false, "???", e.getMessage(), e.getSQLState()));
            throw new UpdateLockException(String.format(EXCEPTION_MESSAGE_UPDATE_LOCK_FAILED, email));
        }
    }

    @Override
    public int incrementLoginAttempts(final String email) throws UpdateLoginAttemptsException {
        try {
            logger.info(String.format(LOG_FORMAT, LOGIN_ATTEMPTS_INCREMENTED, email));
            return executeSqlUpdateOnAccount(email, SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT, usersDB);

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_INCREMENT_LOGIN_ATTEMPTS_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new UpdateLoginAttemptsException(String.format(EXCEPTION_MESSAGE_UPDATE_LOGIN_ATTEMPTS_FAILED, email));
        }
    }

    @Override
    public int clearLoginAttempts(final String email) throws UpdateLoginAttemptsException {
        try {
            logger.info(String.format(LOG_FORMAT, LOGIN_ATTEMPTS_CLEARED, email));
            return executeSqlUpdateOnAccount(email, SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR, usersDB);

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_CLEAR_LOGIN_ATTEMPTS_FAILED, email, e.getMessage(), e.getSQLState()));
            throw new UpdateLoginAttemptsException(String.format(EXCEPTION_MESSAGE_UPDATE_LOGIN_ATTEMPTS_FAILED, email));
        }
    }

    @Override
    public boolean login(final String email, final String password) throws
            AccountDoesNotExistException,
            AccountLockedException,
            AccountEmailNotVerifiedException {

        try (final Connection c = usersDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_SELECT_ACCOUNT_FOR_LOGIN)) {
            ps.setString(1, email.toLowerCase());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                final boolean locked      = rs.getBoolean("account_locked");
                final boolean verified    = rs.getBoolean("email_address_verified");
                final int loginAttempts   = rs.getInt    ("account_login_attempts");
                final String passwordHash = rs.getString ("password_hash");

                if (locked) {
                    throw new AccountLockedException(email);
                }

                if (! verified) {
                    throw new AccountEmailNotVerifiedException(email);
                }

                if (passwordHash.equals(Crypt.crypt(password, passwordHash))) {
                    if (loginAttempts > 0) {
                        try { clearLoginAttempts(email); }
                        catch (final UpdateLoginAttemptsException ignored){}
                    }

                    logger.info(String.format(LOG_FORMAT, LOGGED_IN, email));
                    return true;

                } else {
                    logger.info(String.format(LOG_FORMAT, LOGIN_FAILED, email));
                    if (loginAttempts < Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key))) {
                        try { incrementLoginAttempts(email); }
                        catch (final UpdateLoginAttemptsException ignored) {}

                    } else {
                        try { lock(email, "Maximum login attempts reached."); }
                        catch (final UpdateLockException ignored) {}
                    }

                    return false;
                }

            } else {
                throw new AccountDoesNotExistException(email);
            }

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_LOG_ACCOUNT_GET_FAILED, email, e.getMessage(), e.getSQLState()));
            return false;
        }
    }
}
