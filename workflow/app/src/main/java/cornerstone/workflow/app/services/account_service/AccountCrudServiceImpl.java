package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.rest.account.AccountLoginJsonDto;
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

public class AccountCrudServiceImpl implements AccountCrudService {
    private static final Logger logger = LoggerFactory.getLogger(AccountCrudServiceImpl.class);
    private final BasicDataSource dataSource;

    private static final String GET_ACCOUNT_ERROR_MESSAGE              = "Failed to get account: '%s', message: '%s', SQL state '%s'";
    private static final String CREATE_ACCOUNT_ERROR_MESSAGE           = "Failed to create account: '%s', message: '%s', SQL state: '%s'";
    private static final String DELETE_ACCOUNT_ERROR_MESSAGE           = "Failed to delete account: '%s', message: '%s', SQL state: '%s'";
    private static final String PASSWORD_CHANGE_ERROR_MESSAGE          = "Failed to change password for account: '%s', message: '%s', SQL state: '%s'";
    private static final String EMAIL_ADDRESS_CHANGE_ERROR_MESSAGE     = "Failed to change email address of account: '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ACCOUNT_AVAILABLE_CHANGE_ERROR_MESSAGE = "Failed to change availability of account: '%s to '%s'', message: '%s', SQL state: '%s'";

    private static final String SQL_GET_ACCOUNT                   = "SELECT * FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_CREATE_ACCOUNT                = "INSERT INTO info.accounts (password_hash, email_address, account_available, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT                = "DELETE FROM info.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD       = "UPDATE info.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS  = "UPDATE info.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_ACCOUNT_ENABLE                = "UPDATE info.accounts SET account_available=true, account_disable_reason=null WHERE email_address=(?)";
    private static final String SQL_ACCOUNT_DISABLE               = "UPDATE info.accounts SET account_available=false, account_disable_reason=(?) WHERE email_address=(?)";

    @Inject
    public AccountCrudServiceImpl(final AccountDB AccountDB) {
        this.dataSource = AccountDB;
    }

    @Override
    public AccountResultSetDto getAccount(final String emailAddress) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_GET_ACCOUNT)) {
                ps.setString(1, emailAddress.toLowerCase());
                final ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    final AccountResultSetDto dto = new AccountResultSetDto();
                    dto.set_account_id(rs.getInt("account_id"));
                    dto.set_account_registration_ts(rs.getTimestamp("account_registration_ts"));
                    dto.set_account_available(rs.getBoolean("account_available"));
                    dto.set_account_available_ts(rs.getTimestamp("account_available_ts"));
                    dto.set_account_disable_reason(rs.getString("account_disable_reason"));
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
            final String msg = String.format(GET_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }

    @Override
    public int createAccount(final String emailAddress, final String password, final boolean available) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress.toLowerCase());
                ps.setBoolean(3, available);
                ps.setBoolean(4, false);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }

    @Override
    public int createAccounts(final List<AccountLoginJsonDto> accounts) throws AccountCrudServiceBulkException {
        AccountCrudServiceBulkException accountCrudServiceBulkException = null;
        int updates = 0;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                for (final AccountLoginJsonDto account : accounts) {
                    if ( null != account ) {
                        try {
                            ps.setString(1, Crypt.crypt(account.getPassword()));
                            ps.setString(2, account.getEmail().toLowerCase());
                            ps.setBoolean(3, true);
                            ps.setBoolean(4, false);

                            updates += ps.executeUpdate();

                        } catch (final SQLException e) {
                            final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, account.getEmail(), e.getMessage(), e.getSQLState());
                            logger.error(msg);

                            if ( null == accountCrudServiceBulkException) {
                                accountCrudServiceBulkException = new AccountCrudServiceBulkException();
                            }

                            accountCrudServiceBulkException.addException(new AccountCrudServiceException(e.getMessage()));
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountCrudServiceBulkException) {
                accountCrudServiceBulkException = new AccountCrudServiceBulkException();
            }

            accountCrudServiceBulkException.addException(new AccountCrudServiceException(e.getMessage()));
        }

        if ( null != accountCrudServiceBulkException) {
            throw accountCrudServiceBulkException;
        } else {
            return updates;
        }
    }

    @Override
    public int deleteAccount(final String emailAddress) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                ps.setString(1, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }

    @Override
    public int deleteAccounts(final List<String> emailAddresses) throws AccountCrudServiceBulkException {
        AccountCrudServiceBulkException accountCrudServiceBulkException = null;
        int updates = 0;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                for (final String email : emailAddresses) {
                    try {
                        ps.setString(1, email);

                        updates += ps.executeUpdate();

                    } catch (final SQLException e) {
                        final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, email, e.getMessage(), e.getSQLState());
                        logger.error(msg);

                        if ( null == accountCrudServiceBulkException) {
                            accountCrudServiceBulkException = new AccountCrudServiceBulkException();
                        }

                        accountCrudServiceBulkException.addException(new AccountCrudServiceException(e.getMessage()));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountCrudServiceBulkException) {
                accountCrudServiceBulkException = new AccountCrudServiceBulkException();
            }

            accountCrudServiceBulkException.addException(new AccountCrudServiceException(e.getMessage()));
        }

        if ( null != accountCrudServiceBulkException) {
            throw accountCrudServiceBulkException;
        } else {
            return updates;
        }
    }

    @Override
    public int setAccountPassword(final String emailAddress, final String password) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)){
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e){
            final String msg = String.format(PASSWORD_CHANGE_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(msg);
        }
    }

    @Override
    public int setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {
                ps.setString(1, newEmailAddress);
                ps.setString(2, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(EMAIL_ADDRESS_CHANGE_ERROR_MESSAGE, emailAddress, newEmailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }

    @Override
    public int enableAccount(final String emailAddress) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_ACCOUNT_ENABLE)) {
                ps.setString(1, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ACCOUNT_AVAILABLE_CHANGE_ERROR_MESSAGE, emailAddress, true, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }

    @Override
    public int disableAccount(final String emailAddress, final String reason) throws AccountCrudServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_ACCOUNT_DISABLE)) {
                ps.setString(1, reason);
                ps.setString(2, emailAddress);

                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ACCOUNT_AVAILABLE_CHANGE_ERROR_MESSAGE, emailAddress, false, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountCrudServiceException(e.getMessage());
        }
    }
}
