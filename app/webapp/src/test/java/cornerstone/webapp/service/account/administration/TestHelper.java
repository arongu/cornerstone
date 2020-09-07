package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.rest.endpoint.account.DeletionException;
import cornerstone.webapp.service.account.administration.exceptions.single.NoAccountException;

public class TestHelper {
    public static void deleteAccount(final AccountManager accountManager, final String email){
        try {
            accountManager.delete(email);
        } catch (final DeletionException | NoAccountException ignored) {
        }
    }
}
