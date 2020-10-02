package cornerstone.webapp.services.account.management;

import cornerstone.webapp.services.account.management.exceptions.single.DeletionException;
import cornerstone.webapp.services.account.management.exceptions.single.NoAccountException;

public class TestHelper {
    public static void deleteAccount(final AccountManager accountManager, final String email){
        try {
            accountManager.delete(email);
        } catch (final DeletionException | NoAccountException ignored) {
        }
    }
}
