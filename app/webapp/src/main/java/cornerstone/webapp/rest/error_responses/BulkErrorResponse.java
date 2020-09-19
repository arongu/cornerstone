package cornerstone.webapp.rest.general;
/*
    {
        "httpStatusCode": 200/400/500,
        "status": "failed/incomplete/complete",
        "errors": null,[]
    }
*/

import java.util.List;

public class BulkErrorResponse extends TemplateErrorResponse {
    public enum STATUS {
        FAILED,
        INCOMPLETE,
        COMPLETE;
    }

    private List<String> errors;
    private STATUS status;

    public BulkErrorResponse() {
        super();
    }

    public BulkErrorResponse(final int httpStatusCode, final STATUS status, final List<String> errors) {
        super(httpStatusCode);
        this.status = status;
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
