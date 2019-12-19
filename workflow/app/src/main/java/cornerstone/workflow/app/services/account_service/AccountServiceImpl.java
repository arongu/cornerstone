package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.rest.account.AccountDTO;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final BasicDataSource dataSource;

    private static final String CREATE_ACCOUNT_ERROR_MESSAGE = "Failed to create account: '%s', message: '%s', SQL state: '%s'";
    private static final String DELETE_ACCOUNT_ERROR_MESSAGE = "Failed to delete account: '%s', message: '%s', SQL state: '%s'";
    private static final String PASSWORD_UPDATE_ERROR_MESSAGE = "Failed to update password for account: '%s', message: '%s', SQL state: '%s'";
    private static final String EMAIL_ADDRESS_CHANGE_ERROR_MESSAGE = "Failed to change email address of account: '%s' to '%s', message: '%s', SQL state: '%s'";
    private static final String ACCOUNT_ENABLED_UPDATE_ERROR_MESSAGE = "Failed to change enabled of account: '%s to '%s'', message: '%s', SQL state: '%s'";

    private static final String SQL_CREATE_ACCOUNT = "INSERT INTO accounts_schema.accounts (password_hash, email_address, account_enabled, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT = "DELETE FROM accounts_schema.accounts WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_PASSWORD = "UPDATE accounts_schema.accounts SET password_hash=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS  = "UPDATE accounts_schema.accounts SET email_address=(?) WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_ENABLE = "UPDATE accounts_schema.accounts SET accounts_enabled=true, account_disable_reason=null WHERE email_address=(?)";
    private static final String SQL_UPDATE_ACCOUNT_DISABLE = "UPDATE accounts_schema.accounts SET accounts_enabled=false, account_disable_reason=(?) WHERE email_address=(?)";

    @Inject
    public AccountServiceImpl(final AccountDB AccountDB) {
        this.dataSource = AccountDB;
    }

    @Override
    public void createAccount(final String emailAddress, final String password) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress.toLowerCase());
                ps.setBoolean(3, true);
                ps.setBoolean(4, false);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public void createAccounts(final List<AccountDTO> accounts) throws AccountServiceBulkException {
        AccountServiceBulkException accountServiceBulkException = null;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT)) {
                for (AccountDTO account : accounts) {
                    if ( null != account ) {
                        try {
                            ps.setString(1, Crypt.crypt(account.getPassword()));
                            ps.setString(2, account.getEmail().toLowerCase());
                            ps.setBoolean(3, true);
                            ps.setBoolean(4, false);
                            ps.executeUpdate();
                        } catch (final SQLException e) {
                            final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, account.getEmail(), e.getMessage(), e.getSQLState());
                            logger.error(msg);

                            if ( null == accountServiceBulkException ) {
                                accountServiceBulkException = new AccountServiceBulkException();
                            }

                            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountServiceBulkException ) {
                accountServiceBulkException = new AccountServiceBulkException();
            }
            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if ( null != accountServiceBulkException ) {
            throw accountServiceBulkException;
        }
    }

    @Override
    public void deleteAccount(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                ps.setString(1, emailAddress);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public void deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException {
        AccountServiceBulkException accountServiceBulkException = null;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                for (String email : emailAddresses) {
                    try {
                        ps.setString(1, email);
                        ps.executeUpdate();
                    } catch (final SQLException e) {
                        final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, email, e.getMessage(), e.getSQLState());
                        logger.error(msg);

                        if ( null == accountServiceBulkException ) {
                            accountServiceBulkException = new AccountServiceBulkException();
                        }
                        accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( null == accountServiceBulkException ) {
                accountServiceBulkException = new AccountServiceBulkException();
            }
            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if (accountServiceBulkException != null) {
            throw accountServiceBulkException;
        }
    }

    @Override
    public void setAccountPassword(final String emailAddress, final String password) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_PASSWORD)){
                ps.setString(1, Crypt.crypt(password));
                ps.setString(2, emailAddress);
                ps.execute();
            }
        } catch (final SQLException e){
            final String msg = String.format(PASSWORD_UPDATE_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(msg);
        }
    }

    @Override
    public void setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_EMAIL_ADDRESS)) {
                ps.setString(1, emailAddress);
                ps.setString(2, newEmailAddress);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(EMAIL_ADDRESS_CHANGE_ERROR_MESSAGE, emailAddress, newEmailAddress, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public void enableAccount(final String emailAddress) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_ENABLE)) {
                ps.setString(1, emailAddress);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ACCOUNT_ENABLED_UPDATE_ERROR_MESSAGE, emailAddress, true, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public void disableAccount(final String emailAddress, final String reason) throws AccountServiceException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCOUNT_DISABLE)) {
                ps.setString(1, reason);
                ps.setString(2, emailAddress);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            final String msg = String.format(ACCOUNT_ENABLED_UPDATE_ERROR_MESSAGE, emailAddress, false, e.getMessage(), e.getSQLState());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }
}
