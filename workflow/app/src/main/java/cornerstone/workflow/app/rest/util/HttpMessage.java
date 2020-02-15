package cornerstone.workflow.app.rest.util;

public final class HttpMessage {
    public final String httpStatusMessage;
    public final int httpStatusCode;

    public HttpMessage(final String httpStatusMessage, final int httpStatusCode) {
        this.httpStatusMessage = httpStatusMessage;
        this.httpStatusCode = httpStatusCode;
    }
}
