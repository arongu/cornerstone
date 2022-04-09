package cornerstone.webapp.services.accounts.management.exceptions.account.common;

public class ParameterNotSetException extends Exception {
    static private final String message = "Unset parameter: '%s'";

    public ParameterNotSetException() {
    }

    public ParameterNotSetException(final String parameterName) {
        super(String.format(message, parameterName));
    }
}
