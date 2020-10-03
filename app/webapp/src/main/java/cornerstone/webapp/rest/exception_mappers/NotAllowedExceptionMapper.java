package cornerstone.webapp.rest.exception_mappers;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(NotAllowedExceptionMapper.class);
    private static final String message = "Method not allowed exception caught! class name: '%s', message: '%s'";

    @Override
    public Response toResponse(final NotAllowedException e) {
        final ErrorResponse errorResponse = new ErrorResponse(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), "Method not allowed.");
        logger.info(String.format(message, e.getClass().getName(), e.getMessage()));

        return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
