package cornerstone.webapp.rest.exception_mappers.http;

import cornerstone.webapp.rest.error_responses.ErrorResponse;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
@Priority(500)
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {
    @Override
    public Response toResponse(final NotAllowedException e) {
        final ErrorResponse errorResponse = new ErrorResponse(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), "Method not allowed.");

        return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
