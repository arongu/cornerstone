package cornerstone.webapp.common;

import cornerstone.webapp.services.rsa.rotation.KeyRotationTask;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorImpl;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;

public final class LogMessageLines {
    // simple class to make the logging fields readable
    // %s <- spaces to move "in" / --> the text
    // %30s fixed width for text right aligned
    // %s to display and data
    private LogMessageLines(){};
    private static final String offset_string = " ";

    public static final HashMap<String, Integer> classNameOffsets;
    public static final HashMap<String, String> classNameOffsetSpaces;
    public static int longestClassNameLength = 0;

    static {
        // list of classes to be aligned
        classNameOffsets = new HashMap<>();
        classNameOffsets.put(PublicKeyStoreImpl.class.getName(), null);
        classNameOffsets.put(LocalKeyStoreImpl.class.getName(), null);
        classNameOffsets.put(KeyRotatorImpl.class.getName(), null);
        classNameOffsets.put(KeyRotationTask.class.getName(), null);


        // generate offset spaces
        classNameOffsets.keySet().forEach(className -> {
            final int length = className.length();
            if (length > longestClassNameLength) {
                longestClassNameLength = length;
            }
        });

        classNameOffsets.keySet().forEach(className -> {
            classNameOffsets.put(className, longestClassNameLength - className.length());
        });

        classNameOffsetSpaces = new HashMap<>();
        classNameOffsets.forEach((key, value) -> classNameOffsetSpaces.put(key, CharBuffer.allocate(value).toString().replace('\0', ' ')));
    };

    public static final String MESSAGE_FORMAT_SPACES_FIELD1                 = "%s%-50s";
    public static final String MESSAGE_FORMAT_SPACES_FIELD1_DATA            = "%s%-50s : %s";
    public static final String MESSAGE_FORMAT_SPACES_FIELD1_DATA1_DATA2     = "%s%-50s : %s, %s";
}
