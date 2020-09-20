package cornerstone.webapp.rest.error_responses;
/*
    {
        "httpStatusCode": 200/400/500,
        "error": null/"error string"
    }
*/

public class SingleErrorResponse extends TemplateErrorResponse {
    private String error;

    public SingleErrorResponse() {
        super();
    }

    public SingleErrorResponse(final int httpStatusCode, final String error) {
        super(httpStatusCode);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }
}
