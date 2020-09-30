package cornerstone.webapp.rest.error_responses;
/*
    {
        "httpStatusCode": 200/400/500,
        "status": "failed/incomplete/complete",
        "errors": null,[]
    }
*/

import java.util.List;

public class MultiErrorResponse extends TemplateErrorResponse {
    private List<String> errors;

    public MultiErrorResponse() {
        super();
    }

    public MultiErrorResponse(final int httpStatusCode, final List<String> errors) {
        super(httpStatusCode);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
