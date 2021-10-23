package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.services.accounts.management.exceptions.single.DeletionException;
import cornerstone.webapp.services.accounts.management.exceptions.single.NoAccountException;

public class TestHelper {
    public static void deleteAccount(final AccountManager accountManager, final String email){
        try {
            accountManager.delete(email);
        } catch (final DeletionException | NoAccountException ignored) {

        }
    }
}
