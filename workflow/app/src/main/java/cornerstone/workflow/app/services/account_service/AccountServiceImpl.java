package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.rest.endpoints.account.AccountDTO;
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

    private static final String CREATE_ACCOUNT_ERROR_MESSAGE = "Failed to create account: '%s', message: '%s', error code: '%d'";
    private static final String DELETE_ACCOUNT_ERROR_MESSAGE = "Failed to delete account: '%s' due to the following error: '%s'";

    private static final String SQL_CREATE_ACCOUNT = "INSERT INTO accounts (password_hash, email_address, account_enabled, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT = "DELETE FROM accounts WHERE email_address=(?)";

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
            final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage(), e.getErrorCode());
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
                    try {
                        ps.setString(1, Crypt.crypt(account.getPassword()));
                        ps.setString(2, account.getEmail().toLowerCase());
                        ps.setBoolean(3, true);
                        ps.setBoolean(4, false);
                        ps.executeUpdate();
                    }
                    catch (final SQLException e) {
                        final String msg = String.format(CREATE_ACCOUNT_ERROR_MESSAGE, account.getEmail(), e.getMessage(), e.getErrorCode());
                        logger.error(msg);

                        if (accountServiceBulkException == null) {
                            accountServiceBulkException = new AccountServiceBulkException();
                        }
                        accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( accountServiceBulkException == null ){
                accountServiceBulkException = new AccountServiceBulkException();
            }
            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if (accountServiceBulkException != null) {
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
            final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, emailAddress, e.getMessage());
            logger.error(msg);

            throw new AccountServiceException(e.getMessage());
        }
    }

    @Override
    public void deleteAccounts(List<String> emailAddresses) throws AccountServiceBulkException {
        AccountServiceBulkException accountServiceBulkException = null;

        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
                for (String email : emailAddresses) {
                    try {
                        ps.setString(1, email);
                        ps.executeUpdate();
                    } catch (final SQLException e) {
                        final String msg = String.format(DELETE_ACCOUNT_ERROR_MESSAGE, email, e.getMessage());
                        logger.error(msg);

                        if (accountServiceBulkException == null) {
                            accountServiceBulkException = new AccountServiceBulkException();
                        }
                        accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());

            if ( accountServiceBulkException == null ){
                accountServiceBulkException = new AccountServiceBulkException();
            }
            accountServiceBulkException.addException(new AccountServiceException(e.getMessage()));
        }

        if (accountServiceBulkException != null) {
            throw accountServiceBulkException;
        }
    }

//    @Override
//    public boolean changeAccountPassword(final String email, final String password) {
//        final String sqlUpdate = "UPDATE accounts SET password=(?) WHERE email=(?);";
//
//        try (Connection c = DBpool.getAdminDbConnection()) {
//            try (PreparedStatement ps = c.prepareStatement(sqlUpdate)){
//                ps.setString(1, Crypt.crypt(password));
//                ps.setString(2, email);
//                return ps.executeUpdate() == 1;
//            }
//        }
//        catch (SQLException e){
//            logger.error("Failed to update password for account : '{}' due to the following error: '{}'", email, e.getMessage());
//        }
//
//        return false;
//    }
}