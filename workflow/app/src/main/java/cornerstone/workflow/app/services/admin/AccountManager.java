package cornerstone.workflow.app.services.admin;

import cornerstone.workflow.app.rest.endpoint.admin.AccountDTO;

import java.sql.SQLException;
import java.util.List;

public interface AccountManager {
    void createAccount(final String email, final String password);
    void createAccounts(final List<AccountDTO> accountDTOS) throws SQLException;
    void deleteAccount(final String email);
    void deleteAccounts(final List<String> emails);
// TODO
    //    boolean changeAccountPassword(final String email, final String password);
//    boolean changeAccountEmail(final String email);
//    enable/disableAccount
}
