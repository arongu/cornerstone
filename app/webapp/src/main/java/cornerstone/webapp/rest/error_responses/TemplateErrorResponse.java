package cornerstone.webapp.rest.error_responses;

public abstract class TemplateErrorResponse {
    private int httpStatusCode;

    public TemplateErrorResponse() {
    }

    public TemplateErrorResponse(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
