package cornerstone.workflow.app.rest;

public final class RestMessageTemplate {
    public static String getHttpStatusMessageAsJSON(final String httpStatusMessage, final int httpStatusCode) {
        return "{\"httpStatusMessage\":\"" + httpStatusMessage + "\",\"httpStatusCode\":" + httpStatusCode + "}";
    }
}
