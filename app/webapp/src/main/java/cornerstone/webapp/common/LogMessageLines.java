package cornerstone.webapp.common;

import cornerstone.webapp.services.rsa.rotation.KeyRotationTask;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorImpl;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;

import java.nio.CharBuffer;
import java.util.HashMap;

// Simple class to make the log fields aligned and readable
public final class LogMessageLines {
    private LogMessageLines(){};

    public static final HashMap<String, String> classNameOffsetSpaces;

    static {
        // ADD YOUR CLASS HERE
        // USE THE OFFSET LENGTH FROM THE MAP IN THE LOG STRING
        // e.g.: String.format(LogMessageLines.classNameOffsetSpaces.get(<one of the aligned class names>), <data/text>);
        String[] alignedClasses = {
                PublicKeyStoreImpl.class.getName(),
                LocalKeyStoreImpl.class.getName(),
                KeyRotatorImpl.class.getName(),
                KeyRotationTask.class.getName()
        };

        int longestClassNameLength = 0;
        for (final String className : alignedClasses) {
            if (className.length() > longestClassNameLength){
                longestClassNameLength = className.length();
            }
        }

        classNameOffsetSpaces = new HashMap<>();
        for (final String className : alignedClasses) {
            int offset_length = longestClassNameLength - className.length();
            classNameOffsetSpaces.put(className, CharBuffer.allocate(offset_length).toString().replace('\0', ' '));
        }
    };

    public static final String SPACES_FIELD                           = "%s%-60s";
    public static final String SPACES_FIELD__DATA                     = "%s%-60s : %s";
    public static final String SPACES_FIELD__DATA__DATA               = "%s%-60s : %s, %s";
    public static final String SPACES__STATUS_WORD__FIELD             = "%s%-30s%-30s";
    public static final String SPACES__STATUS_WORD__FIELD__DATA       = "%s%-30s%-30s : %s";
    public static final String SPACES__STATUS_WORD__FIELD__DATA__DATA = "%s%-30s%-30s : %s, %s";

}
