package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import org.apache.commons.codec.digest.Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class AccountManagerImpl implements AccountManager {
    private static final String LOG_FORMAT = "%-35s : %s";

    private static final String SQL_SELECT_ACCOUNT                  = "SELECT * FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_SELECT_ACCOUNT_FOR_LOGIN        = "SELECT account_locked, email_address_verified, account_login_attempts, password_hash FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_INSERT_ACCOUNT                  = "INSERT INTO user_data.accounts (password_hash, email_address, account_locked, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                  = "DELETE FROM user_data.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD         = "UPDATE user_data.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS    = "UPDATE user_data.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED           = "UPDATE user_data.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT = "UPDATE user_data.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR     = "UPDATE user_data.accounts SET account_login_attempts=0 WHERE email_address=(?)";

    private static final String ERROR_MESSAGE_FAILED_TO_RETRIEVE_ACCOUNT         = "Failed to retrieve account: '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_CREATE_ACCOUNT           = "Failed to create account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_DELETE_ACCOUNT           = "Failed to delete account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_CHANGE_PASSWORD          = "Failed to change password for account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_CHANGE_ADDRESS           = "Failed to change email address of account: '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_CHANGE_ACCOUNT_LOCK      = "Failed to change account_locked of account: '%s' to '%s', account_lock_reason '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_INCREMENT_LOGIN_ATTEMPTS = "Failed to increment login attempts of account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_CLEAR_LOGIN_ATTEMPTS     = "Failed to clear login attempts of account: '%s', message: '%s', SQL state: '%s'";

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

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    private static int executeUpdateWithEmailAddress(final String emailAddress,
                                                     final String sqlStatement,
                                                     final String errorMessage,
                                                     final UsersDB usersDB) throws AccountManagerException {

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(sqlStatement)) {

            ps.setString(1, emailAddress);
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(errorMessage, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    private final UsersDB usersDB;
    private final ConfigurationLoader configurationLoader;

    @Inject
    public AccountManagerImpl(final UsersDB usersDB, final ConfigurationLoader configurationLoader) {
        this.usersDB = usersDB;
        this.configurationLoader = configurationLoader;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public AccountResultSet get(final String emailAddress) throws AccountManagerException, NoSuchElementException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_SELECT_ACCOUNT)) {

            ps.setString(1, emailAddress.toLowerCase());
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info(String.format(LOG_FORMAT, RETRIEVED, emailAddress));
                return new AccountResultSet(
                        rs.getInt("account_id"),
                        rs.getTimestamp("account_registration_ts"),
                        rs.getBoolean("account_locked"),
                        rs.getTimestamp("account_locked_ts"),
                        rs.getString("account_lock_reason"),
                        rs.getInt("account_login_attempts"),
                        rs.getString("email_address"),
                        rs.getTimestamp("email_address_ts"),
                        rs.getBoolean("email_address_verified"),
                        rs.getTimestamp("email_address_verified_ts"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("password_hash_ts")
                );

            } else {
                throw new NoSuchElementException();
            }

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_RETRIEVE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    @Override
    public int create(final String emailAddress,
                      final String password,
                      final boolean accountLocked,
                      final boolean verified) throws AccountManagerException {

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_ACCOUNT)) {

            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, emailAddress.toLowerCase());
            ps.setBoolean(3, accountLocked);
            ps.setBoolean(4, verified);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, CREATED, emailAddress));
            return updatedRows;

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CREATE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    // mostly used for testing
    @Override
    public int create(final List<AccountEmailPassword> emailsAndPasswords) throws AccountManagerMultipleException {
        AccountManagerMultipleException multipleException = null;
        int updatedRows = 0;

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_ACCOUNT)) {

            for (final AccountEmailPassword accountEmailPassword : emailsAndPasswords) {
                if (null != accountEmailPassword) {
                    final String password = accountEmailPassword.getPassword();
                    final String email = accountEmailPassword.getEmail();

                    try {
                        ps.setString(1, Crypt.crypt(password));
                        ps.setString(2, email.toLowerCase());
                        ps.setBoolean(3, true);
                        ps.setBoolean(4, false); // false by default

                        updatedRows += ps.executeUpdate();
                        logger.info(String.format(LOG_FORMAT, CREATED, email));

                    } catch (final SQLException e) {
                        final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CREATE_ACCOUNT, email, e.getMessage(), e.getSQLState());
                        if (null == multipleException) {
                            multipleException = new AccountManagerMultipleException();
                        } else {
                            multipleException.addException(new AccountManagerException(e.getMessage()));
                        }

                        logger.error(msg);
                    }
                }
            }

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            if (null == multipleException) {
                multipleException = new AccountManagerMultipleException();
            } else {
                multipleException.addException(new AccountManagerException(e.getMessage()));
            }
        }

        if (null != multipleException) {
            throw multipleException;
        } else {
            return updatedRows;
        }
    }

    @Override
    public int delete(final String emailAddress) throws AccountManagerException {
        final int deletedRows = executeUpdateWithEmailAddress(emailAddress, SQL_DELETE_ACCOUNT, ERROR_MESSAGE_FAILED_TO_DELETE_ACCOUNT, usersDB);
        logger.info(String.format(LOG_FORMAT, DELETED, emailAddress));

        return deletedRows;
    }

    @Override
    public int delete(final List<String> emailAddresses) throws AccountManagerMultipleException {
        AccountManagerMultipleException multipleException = null;
        int deletedRows = 0;

        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_DELETE_ACCOUNT) ) {

            for (final String email : emailAddresses) {
                try {
                    ps.setString(1, email);
                    deletedRows += ps.executeUpdate();
                    logger.info(String.format(LOG_FORMAT, DELETED, emailAddresses));

                } catch (final SQLException e) {
                    final String msg = String.format(ERROR_MESSAGE_FAILED_TO_DELETE_ACCOUNT, email, e.getMessage(), e.getSQLState());
                    if (null == multipleException) { // create multiException if it does not exist
                        multipleException = new AccountManagerMultipleException();
                    }

                    multipleException.addException(new AccountManagerException(e.getMessage()));
                    logger.error(msg);
                }
            }

        } catch (final SQLException e) {
            if (null == multipleException) {
                multipleException = new AccountManagerMultipleException();
            }

            multipleException.addException(new AccountManagerException(e.getMessage()));
            logger.error(e.getMessage());
        }

        if (null != multipleException) {
            throw multipleException;
        } else {
            return deletedRows;
        }
    }

    @Override
    public int setPassword(final String emailAddress, final String password) throws AccountManagerException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)) {

            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, emailAddress);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, PASSWORD_CHANGED, emailAddress));
            return updatedRows;

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CHANGE_PASSWORD, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(msg);
        }
    }

    @Override
    public int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountManagerException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {

            ps.setString(1, newEmailAddress);
            ps.setString(2, emailAddress);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format("%-35s : %s -> %s", EMAIL_ADDRESS_CHANGED, emailAddress, newEmailAddress));
            return updatedRows;

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CHANGE_ADDRESS, emailAddress, newEmailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    @Override
    public int lock(final String emailAddress, final String reason) throws AccountManagerException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {

            ps.setBoolean(1,true);
            ps.setString(2, reason);
            ps.setString(3, emailAddress);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, LOCKED, emailAddress));
            return updatedRows;

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CHANGE_ACCOUNT_LOCK, emailAddress, true, reason, e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    @Override
    public int unlock(final String emailAddress) throws AccountManagerException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {

            ps.setBoolean(1,false);
            ps.setString(2, null);
            ps.setString(3, emailAddress);
            final int updatedRows = ps.executeUpdate();

            logger.info(String.format(LOG_FORMAT, UNLOCKED, emailAddress));
            return updatedRows;

        } catch ( final SQLException e ) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_CHANGE_ACCOUNT_LOCK, emailAddress, false, "", e.getMessage(), e.getSQLState());
            logger.error(msg);
            throw new AccountManagerException(e.getMessage());
        }
    }

    @Override
    public int incrementLoginAttempts(final String emailAddress) throws AccountManagerException {
        final int updates = executeUpdateWithEmailAddress(emailAddress, SQL_UPDATE_LOGIN_ATTEMPTS_INCREMENT, ERROR_MESSAGE_FAILED_TO_INCREMENT_LOGIN_ATTEMPTS, usersDB);
        logger.info(String.format(LOG_FORMAT, LOGIN_ATTEMPTS_INCREMENTED, emailAddress));
        return updates;
    }

    @Override
    public int clearLoginAttempts(final String emailAddress) throws AccountManagerException {
        final int updates = executeUpdateWithEmailAddress(emailAddress, SQL_UPDATE_LOGIN_ATTEMPTS_CLEAR, ERROR_MESSAGE_FAILED_TO_CLEAR_LOGIN_ATTEMPTS, usersDB);
        logger.info(String.format(LOG_FORMAT, LOGIN_ATTEMPTS_CLEARED, emailAddress));
        return updates;
    }

    @Override
    public boolean login(final String emailAddress, final String password) throws AccountManagerException {
        try (final Connection c = usersDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_SELECT_ACCOUNT_FOR_LOGIN)) {

            ps.setString(1, emailAddress.toLowerCase());
            final ResultSet rs = ps.executeQuery();
            if (rs != null && rs.next()) {
                final boolean locked = rs.getBoolean("account_locked");
                final boolean verified = rs.getBoolean("email_address_verified");
                final int loginAttempts = rs.getInt("account_login_attempts");
                final String passwordHash = rs.getString("password_hash");

                if (! locked && verified) {
                    final String reconstructedPasswordHash = Crypt.crypt(password, passwordHash);
                    if (Objects.equals(passwordHash, reconstructedPasswordHash)) {
                        if (loginAttempts > 0) {
                            clearLoginAttempts(emailAddress);
                        }
                        logger.info(String.format(LOG_FORMAT, LOGGED_IN, emailAddress));
                        return true;

                    } else {
                        logger.info(String.format(LOG_FORMAT, LOGIN_FAILED, emailAddress));
                        if (loginAttempts < Integer.parseInt(configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key))) {
                            incrementLoginAttempts(emailAddress);
                        } else {
                            lock(emailAddress, "Maximum login attempts reached.");
                        }
                    }
                }
            }

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_RETRIEVE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
        }

        return false;
    }
}
