package cornerstone.webapp.rest.exception_mappers.security;

import cornerstone.webapp.rest.error_responses.ErrorResponse;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
@Priority(500)
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
    @Override
    public Response toResponse(final ForbiddenException e) {
        final ErrorResponse errorResponse = new ErrorResponse(Response.Status.FORBIDDEN.getStatusCode(), "Not authorized.");

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
