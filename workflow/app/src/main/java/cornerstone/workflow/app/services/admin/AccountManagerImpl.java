package cornerstone.workflow.app.services.admin;

import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.rest.endpoint.admin.AccountDTO;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AccountManagerImpl implements AccountManager {
    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);
    private final BasicDataSource dataSource;

    private static final String SQL_CREATE_ACCOUNT = "INSERT INTO accounts (password_hash, email_address, account_enabled, email_address_verified) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_ACCOUNT = "DELETE FROM accounts WHERE email_address=(?)";

    @Inject
    public AccountManagerImpl(final AccountDB AccountDB) {
        this.dataSource = AccountDB;
    }

    @Override
    public void createAccount(final String email, final String password) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_CREATE_ACCOUNT)) {
                ps.setString(1, Crypt.crypt(password));;
                ps.setString(2, email.toLowerCase());
                ps.setBoolean(3, true);
                ps.setBoolean(4, false);
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            // TODO throw exception to send JSON error
            logger.error("Failed to create account: '{}' due to the following error: '{}' errorCode: '{}'", email, e.getMessage(), e.getErrorCode());
        }
    }

    @Override
    public void createAccounts(final List<AccountDTO> accountDTOS) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_CREATE_ACCOUNT)) {
                List<AccountManagerError> errors = null;

                for (AccountDTO accountDTO : accountDTOS) {
                    try {
                        ps.setString(1, Crypt.crypt(accountDTO.getPassword()));
                        ps.setString(2, accountDTO.getEmail().toLowerCase());
                        ps.setBoolean(3, true);
                        ps.setBoolean(4, false);
                        ps.executeUpdate();
                    }
                    catch (final SQLException e) {
                        logger.error(e.getMessage());
                        if (errors != null) {
                            errors.add(new AccountManagerError(accountDTO.getEmail(), e.getMessage()));
                        } else {
                            errors = new LinkedList<>();
                        }
                    }
                }

                if (errors != null) {
                    // TODO throw exception here
                    // TODO send back json of the failed inserts
                }
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());
            throw  e;
        }
    }

    @Override
    public void deleteAccount(final String email) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_DELETE_ACCOUNT)) {
                ps.setString(1, email);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            // TODO throw and send json
            logger.error("Failed to delete account: '{}' due to the following error: '{}'", email, e.getMessage());
        }
    }

    @Override
    public void deleteAccounts(List<String> emails) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_DELETE_ACCOUNT)) {
                for (String email : emails) {
                    try {
                        ps.setString(1, email);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        logger.error("Failed to delete account: '{}' due to the following error: '{}'", email, e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("aaaaaaaaa" + e.getMessage());
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