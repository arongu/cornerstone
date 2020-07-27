package cornerstone.webapp.services.rsa.store;

public final class LoggingMessageFormats {
    // simple class to make the logging fields readable
    // %s <- spaces to move "in" / --> the text
    // %30s fixed width for text right aligned
    // %s to display and data
    private LoggingMessageFormats(){};

    public static final String MESSAGE_FORMAT_SPACES_FIELD1                 = "%s%30s";
    public static final String MESSAGE_FORMAT_SPACES_FIELD1_DATA            = "%s%30s : %s";
}
