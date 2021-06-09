package cornerstone.webapp.exception_mappers;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This mapper is a fail safe, a last resort if any exception falls through that should have been handled in the code!
 * Returns HTTP 500
 */

@Singleton
@Provider
@Priority(500)
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    private static final String message = "Unexpected exception caught! class name: '%s', message: '%s'";

    @Override
    public Response toResponse(final Exception e) {
        logger.error(String.format(message, e.getClass().getName(), e.getMessage()));

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ":("))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
