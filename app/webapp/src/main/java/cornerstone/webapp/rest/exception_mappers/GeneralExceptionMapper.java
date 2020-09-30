package cornerstone.webapp.rest.exception_mappers;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
@Priority(500)
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(GeneralExceptionMapper.class);
    private static final String message = "Unexpected exception caught! class name: '%s', message: '%s'";

    @Override
    public Response toResponse(final Exception e) {
        logger.error(String.format(message, e.getClass().getCanonicalName(), e.getMessage()));

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ":("))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
