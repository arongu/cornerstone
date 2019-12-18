package cornerstone.workflow.app.rest.rest_messages;

public final class HttpMessage {
    private String httpStatusMessage;
    private int httpStatusCode;

    public HttpMessage(final String httpStatusMessage, final int httpStatusCode) {
        this.httpStatusMessage = httpStatusMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpStatusMessage() {
        return httpStatusMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
