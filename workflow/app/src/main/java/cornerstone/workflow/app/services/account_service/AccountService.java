package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import cornerstone.workflow.app.rest.endpoint.account.EmailAndPassword;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class AccountService implements AccountServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final BasicDataSource dataSource;

    private static final String ERROR_MSG_FAILED_TO_GET_ACCOUNT              = "Failed to get account: '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_MSG_FAILED_TO_CREATE_ACCOUNT           = "Failed to create account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_DELETE_ACCOUNT           = "Failed to delete account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_CHANGE_PASSWORD          = "Failed to change password for account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_CHANGE_ADDRESS           = "Failed to change email address of account: '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_CHANGE_ACCOUNT_LOCK      = "Failed to change account_locked of account: '%s' to '%s', account_lock_reason '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_INCREMENT_LOGIN_ATTEMPTS = "Failed to increment login attempts of account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MSG_FAILED_TO_CLEAR_LOGIN_ATTEMPTS     = "Failed to clear login attempts of account: '%s', message: '%s', SQL state: '%s'";

    private static final String SQL_GET_ACCOUNT                   = "SELECT * FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_GET_ACCOUNT_FOR_LOGIN         = "SELECT account_locked, email_address_verified, account_login_attempts, password_hash FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_CREATE_ACCOUNT                = "INSERT INTO info.accounts (password_hash, email_address, account_locked, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                = "DELETE FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD       = "UPDATE info.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS  = "UPDATE info.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED         = "UPDATE info.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_INCREMENT_LOGIN_ATTEMPTS      = "UPDATE info.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_CLEAR_LOGIN_ATTEMPTS          = "UPDATE info.accounts SET account_login_attempts=0 WHERE email_address=(?)";


    // Utility function, to avoid code duplication
    private static int runExecuteUpdateWithEmailAddress(final String emailAddress,
                                                        final String sqlStatement,
                                                        final String errorMessage,
                                                        final BasicDataSource dataSource) throws AccountServiceException {

        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(sqlStatement) ) {

            ps.setString(1, emailAddress);
            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    errorMessage, emailAddress, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    @Inject
    public AccountService(final DataSourceAccountDB dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public AccountResultSet get(final String emailAddress) throws AccountServiceException {
        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_GET_ACCOUNT) ) {

            ps.setString(1, emailAddress.toLowerCase());

            final ResultSet rs = ps.executeQuery();
            if ( rs.next() ) {
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
                return null;
            }

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_GET_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int create(final String emailAddress,
                      final String password,
                      final boolean accountLocked,
                      final boolean verified) throws AccountServiceException {

        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_CREATE_ACCOUNT) ) {

            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, emailAddress.toLowerCase());
            ps.setBoolean(3, accountLocked);
            ps.setBoolean(4, verified);

            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_CREATE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    // mostly used for testing
    @Override
    public int create(final List<EmailAndPassword> emailsAndPasswords) throws AccountServiceMultipleException {
        AccountServiceMultipleException multipleException = null;
        int updatedRows = 0;

        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_CREATE_ACCOUNT) ) {

            for ( final EmailAndPassword emailAndPassword : emailsAndPasswords ) {
                if ( null != emailAndPassword ) {
                    try {
                        ps.setString(1, Crypt.crypt(emailAndPassword.password));
                        ps.setString(2, emailAndPassword.email.toLowerCase());
                        ps.setBoolean(3, true);
                        ps.setBoolean(4, false); // false by default

                        updatedRows += ps.executeUpdate();

                    } catch ( final SQLException e ) {
                        final String msg = String.format(
                                ERROR_MSG_FAILED_TO_CREATE_ACCOUNT, emailAndPassword.email, e.getMessage(), e.getSQLState()
                        );

                        if ( null == multipleException ) {
                            multipleException = new AccountServiceMultipleException();

                        } else {
                            multipleException.addException(new AccountServiceException(e.getMessage()));
                        }

                        logger.error(msg);
                    }
                }
            }

        } catch ( final SQLException e ) {
            logger.error(e.getMessage());

            if ( null == multipleException ) {
                multipleException = new AccountServiceMultipleException();
            }

            multipleException.addException(
                    new AccountServiceException(e.getMessage())
            );
        }


        if ( null != multipleException ) {
            throw multipleException;
        } else {
            return updatedRows;
        }
    }


    @Override
    public int delete(final String emailAddress) throws AccountServiceException {
        return runExecuteUpdateWithEmailAddress(emailAddress, SQL_DELETE_ACCOUNT, ERROR_MSG_FAILED_TO_DELETE_ACCOUNT, dataSource);
    }

    @Override
    public int delete(final List<String> emailAddresses) throws AccountServiceMultipleException {
        AccountServiceMultipleException multipleException = null;
        int updatedRows = 0;

        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_DELETE_ACCOUNT) ) {

            for ( final String email : emailAddresses ) {
                try {
                    ps.setString(1, email);
                    updatedRows += ps.executeUpdate();

                } catch ( final SQLException e ) {
                    final String msg = String.format(
                            ERROR_MSG_FAILED_TO_DELETE_ACCOUNT, email, e.getMessage(), e.getSQLState()
                    );

                    if ( null == multipleException ) {
                        multipleException = new AccountServiceMultipleException();
                    }

                    multipleException.addException(
                            new AccountServiceException(e.getMessage())
                    );

                    logger.error(msg);
                }
            }

        } catch ( final SQLException e ) {
            if ( null == multipleException ) {
                multipleException = new AccountServiceMultipleException();
            }

            multipleException.addException(
                    new AccountServiceException(e.getMessage())
            );

            logger.error(e.getMessage());
        }

        if ( null != multipleException ) {
            throw multipleException;
        } else {
            return updatedRows;
        }
    }

    @Override
    public int setPassword(final String emailAddress, final String password) throws AccountServiceException {
        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD) ) {

            ps.setString(1, Crypt.crypt(password));
            ps.setString(2, emailAddress);

            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_CHANGE_PASSWORD, emailAddress, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(msg);
        }
    }

    @Override
    public int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException {
        try ( final Connection conn = dataSource.getConnection();
              final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS) ) {

            ps.setString(1, newEmailAddress);
            ps.setString(2, emailAddress);

            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_CHANGE_ADDRESS, emailAddress, newEmailAddress, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int lock(final String emailAddress, final String reason) throws AccountServiceException {
        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED) ) {

            ps.setBoolean(1,true);
            ps.setString(2, reason);
            ps.setString(3, emailAddress);

            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_CHANGE_ACCOUNT_LOCK, emailAddress, true, reason, e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int unlock(final String emailAddress) throws AccountServiceException {
        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED) ) {

            ps.setBoolean(1,false);
            ps.setString(2, null);
            ps.setString(3, emailAddress);

            return ps.executeUpdate();

        } catch ( final SQLException e ) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_CHANGE_ACCOUNT_LOCK, emailAddress, false, "", e.getMessage(), e.getSQLState()
            );

            logger.error(msg);
            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int incrementLoginAttempts(final String emailAddress) throws AccountServiceException {
        return runExecuteUpdateWithEmailAddress(emailAddress, SQL_INCREMENT_LOGIN_ATTEMPTS, ERROR_MSG_FAILED_TO_INCREMENT_LOGIN_ATTEMPTS, dataSource);
    }

    @Override
    public int clearLoginAttempts(final String emailAddress) throws AccountServiceException {
        return runExecuteUpdateWithEmailAddress(emailAddress, SQL_CLEAR_LOGIN_ATTEMPTS, ERROR_MSG_FAILED_TO_CLEAR_LOGIN_ATTEMPTS, dataSource);
    }

    @Override
    public boolean login(final String emailAddress, final String password) throws AccountServiceException {
        try ( final Connection c = dataSource.getConnection();
              final PreparedStatement ps = c.prepareStatement(SQL_GET_ACCOUNT_FOR_LOGIN) ) {

            ps.setString(1, emailAddress.toLowerCase());
            final ResultSet rs = ps.executeQuery();

            if ( rs.next() ) {
                final boolean locked = rs.getBoolean("account_locked");
                final boolean verified = rs.getBoolean("email_address_verified");
                final int loginAttempts = rs.getInt("account_login_attempts");
                final String passwordHash = rs.getString("password_hash");

                if ( ! locked && verified ) {
                    final boolean passwordMatches = Objects.equals(
                            passwordHash, Crypt.crypt(password, passwordHash)
                    );

                    if ( passwordMatches ) {

                        if ( loginAttempts > 0 ) {
                            clearLoginAttempts(emailAddress);
                        }

                        return true;

                    } else {
                        if ( loginAttempts < 180 ) {
                            incrementLoginAttempts(emailAddress);

                        } else {
                            /* TODO create an exception for this, to able to PASS IP address of the origin
                                 so the caller method can handle this */
                            lock(emailAddress, "Maximum login attempts reached.");
                        }
                    }
                }
            }

        } catch ( final SQLException e ) {
            final String msg = String.format(ERROR_MSG_FAILED_TO_GET_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);
        }

        return false;
    }
}
