package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.rest.account.EmailPasswordPair;
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

public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final BasicDataSource dataSource;

    private static final String ERROR_MESSAGE_GET_ACCOUNT               = "Failed to get account: '%s', message: '%s', SQL state '%s'";
    private static final String ERROR_MESSAGE_CREATE_ACCOUNT            = "Failed to create account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_DELETE_ACCOUNT            = "Failed to delete account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_PASSWORD_CHANGE           = "Failed to change password for account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_EMAIL_ADDRESS_CHANGE      = "Failed to change email address of account: '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_ACCOUNT_LOCK_UNLOCK       = "Failed to change account_locked of account: '%s' to '%s', account_lock_reason '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_INCREMENT_LOGIN_ATTEMPTS  = "Failed to increment login attempts of account: '%s', message: '%s', SQL state: '%s'";
    private static final String ERROR_MESSAGE_CLEAR_LOGIN_ATTEMPTS      = "Failed to clear login attempts of account: '%s', message: '%s', SQL state: '%s'";

    private static final String SQL_GET_ACCOUNT                   = "SELECT * FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_CREATE_ACCOUNT                = "INSERT INTO info.accounts (password_hash, email_address, account_locked, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                = "DELETE FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD       = "UPDATE info.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS  = "UPDATE info.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_LOCKED         = "UPDATE info.accounts SET account_locked=(?), account_lock_reason=(?) WHERE email_address=(?)";
    private static final String SQL_INCREMENT_LOGIN_ATTEMPTS      = "UPDATE info.accounts SET account_login_attempts=account_login_attempts+1 WHERE email_address=(?)";
    private static final String SQL_CLEAR_LOGIN_ATTEMPTS          = "UPDATE info.accounts SET account_login_attempts=0 WHERE email_address=(?)";
    private static final String SQL_AUTH_QUERY                    = "SELECT account_locked, account_verified, account_login_attempts, password_hash FROM info.accounts WHERE email_address=(?)";

    @Inject
    public AccountServiceImpl(final AccountDB AccountDB) {
        this.dataSource = AccountDB;
    }

    @Override
    public AccountResultSetDto getAccount(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_GET_ACCOUNT)) {
                ps.setString(1, emailAddress.toLowerCase());
                final ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    final AccountResultSetDto dto = new AccountResultSetDto();
                    dto.set_account_id(rs.getInt("account_id"));
                    dto.set_account_registration_ts(rs.getTimestamp("account_registration_ts"));

                    dto.set_account_locked(rs.getBoolean("account_locked"));
                    dto.set_account_locked_ts(rs.getTimestamp("account_locked_ts"));
                    dto.set_account_lock_reason(rs.getString("account_lock_reason"));
                    dto.set_account_login_attempts(rs.getInt("account_login_attempts"));

                    dto.set_email_address(rs.getString("email_address"));
                    dto.set_email_address_ts(rs.getTimestamp("email_address_ts"));
                    dto.set_email_address_verified(rs.getBoolean("email_address_verified"));

                    dto.set_email_address_verified_ts(rs.getTimestamp("email_address_verified_ts"));
                    dto.set_password_hash(rs.getString("password_hash"));
                    dto.set_password_hash_ts(rs.getTimestamp("password_hash_ts"));

                    return dto;

                } else {
                    return null;
                }
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_GET_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int createAccount(final String emailAddress, final String password, final boolean available) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress.toLowerCase());
                ps.setBoolean(3, available);
                ps.setBoolean(4, false);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_CREATE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int createAccounts(final List<EmailPasswordPair> list) throws AccountServiceBulkException {
        AccountServiceBulkException accountServiceBulkException = null;
        int updates = 0;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                for (final EmailPasswordPair account : list) {
                    if ( null != account ) {
                        try {
                            ps.setString(1, Crypt.crypt(account.getPassword()));
                            ps.setString(2, account.getEmail().toLowerCase());
                            ps.setBoolean(3, true);
                            ps.setBoolean(4, false);

                            updates += ps.executeUpdate();

                        } catch (final SQLException e) {
                            final String msg = String.format(ERROR_MESSAGE_CREATE_ACCOUNT, account.getEmail(), e.getMessage(), e.getSQLState());
                            logger.error(msg);

                            if ( null == accountServiceBulkException) {
                                accountServiceBulkException = new AccountServiceBulkException();
                            }

                            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountServiceBulkException) {
                accountServiceBulkException = new AccountServiceBulkException();
            }

            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if ( null != accountServiceBulkException) {
            throw accountServiceBulkException;
        } else {
            return updates;
        }
    }

    @Override
    public int deleteAccount(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                ps.setString(1, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_DELETE_ACCOUNT, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException {
        AccountServiceBulkException accountServiceBulkException = null;
        int updates = 0;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                for (final String email : emailAddresses) {
                    try {
                        ps.setString(1, email);

                        updates += ps.executeUpdate();

                    } catch (final SQLException e) {
                        final String msg = String.format(ERROR_MESSAGE_DELETE_ACCOUNT, email, e.getMessage(), e.getSQLState());
                        logger.error(msg);

                        if ( null == accountServiceBulkException) {
                            accountServiceBulkException = new AccountServiceBulkException();
                        }

                        accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountServiceBulkException) {
                accountServiceBulkException = new AccountServiceBulkException();
            }

            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if ( null != accountServiceBulkException) {
            throw accountServiceBulkException;
        } else {
            return updates;
        }
    }

    @Override
    public int setAccountPassword(final String emailAddress, final String password) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)){
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e){
            final String msg = String.format(ERROR_MESSAGE_PASSWORD_CHANGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(msg);
        }
    }

    @Override
    public int setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {
                ps.setString(1, newEmailAddress);
                ps.setString(2, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_EMAIL_ADDRESS_CHANGE, emailAddress, newEmailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int lockAccount(final String emailAddress, final String reason) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {
                ps.setBoolean(1,true);
                ps.setString(2, reason);
                ps.setString(3, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_ACCOUNT_LOCK_UNLOCK, emailAddress, true, reason, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int unlockAccount(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_LOCKED)) {
                ps.setBoolean(1,false);
                ps.setString(2, "");
                ps.setString(3, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_ACCOUNT_LOCK_UNLOCK, emailAddress, false, "", e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int incrementFailedLoginAttempts(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_INCREMENT_LOGIN_ATTEMPTS)) {
                ps.setString(1, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_INCREMENT_LOGIN_ATTEMPTS, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public int clearFailedLoginAttempts(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CLEAR_LOGIN_ATTEMPTS)) {
                ps.setString(1, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_CLEAR_LOGIN_ATTEMPTS, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public boolean login(final String emailAddress, final String password) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_AUTH_QUERY)) {
                ps.setString(1, emailAddress.toLowerCase());
                final ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    final boolean accountLocked = rs.getBoolean("account_locked");
                    final boolean accountVerified = rs.getBoolean("account_verified");
                    final int loginAttempts = rs.getInt("account_login_attempts");
                    final String passwordHash = rs.getString("password_hash");

                    if ( !accountLocked && accountVerified) {
                        final boolean passwordMatches = Objects.equals(passwordHash, Crypt.crypt(password, passwordHash));

                        if (!passwordMatches) {
                            if (180 > loginAttempts) {
                                incrementFailedLoginAttempts(emailAddress);
                            } else {
                                lockAccount(emailAddress, "Maximum login attempt reached.");
                            }
                        }

                        return passwordMatches;
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error("Failed to query password for account: '{}' due to the following error: '{}'", emailAddress, e.getMessage());
        }

        return false;
    }
}
