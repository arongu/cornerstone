package cornerstone.webapp.common;

import cornerstone.webapp.rest.endpoints.login.LoginRestService;
import cornerstone.webapp.services.account.administration.AccountManagerImpl;
import cornerstone.webapp.services.rsa.rotation.KeyRotationTask;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorImpl;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

// Class to make group of classes to be aligned in the logs
public final class AlignedLogMessages {
    private AlignedLogMessages(){};
    private static void generateOffsetSpaces(final String[] classNames, final Map<String,String> offsetMap) {
        int longest = 0;
        for (final String className : classNames) {
            if (className.length() > longest){
                longest = className.length();
            }
        }

        for (final String className : classNames) {
            int offset_length = longest - className.length();
            offsetMap.put(className, CharBuffer.allocate(offset_length).toString().replace('\0', ' '));
        }
    }

    // Log message lines
    public static final String FORMAT__OFFSET_STR             = "%s%s";
    public static final String FORMAT__OFFSET_STR_STR         = "%s%s %s";
    public static final String FORMAT__OFFSET_35C_C_STR       = "%s%-35s : %s";
    public static final String FORMAT__OFFSET_35C_35C         = "%s%-35s %-35s";
    public static final String FORMAT__OFFSET_35C_35C_C_STR   = "%s%-35s %-35s : %s";
    public static final String FORMAT__OFFSET_35C_35C_STR_STR = "%s%-35s %-35s : %s, %s";

    public static final HashMap<String, String> OFFSETS_ALIGNED_CLASSES;
    static {
        // Step 1 - CREATE a String array for classes you want to be aligned together when logging
        // Step 2 - CREATE an empty public Map for each group of classes to store the generated spaces for each group member
        //          the space offsets aligns the group members
        // Step 3 - call the generator function
        // Step 4 - create a public map so it can be accessed outside

        // Offset alignment group for Key Store related classes:
        String[] ALIGNED_CLASSES = {
                // KeyStoreClasses
                LocalKeyStoreImpl.class.getName(),
                PublicKeyStoreImpl.class.getName(),
                KeyRotatorImpl.class.getName(),
                KeyRotationTask.class.getName(),
                // Login
                AccountManagerImpl.class.getName(),
                LoginRestService.class.getName()
        };
        OFFSETS_ALIGNED_CLASSES = new HashMap<>();
        generateOffsetSpaces(ALIGNED_CLASSES, OFFSETS_ALIGNED_CLASSES);
    };
}
