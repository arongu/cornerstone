package cornerstone.webapp.rest.exception_mappers;

import com.fasterxml.jackson.core.JsonParseException;
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
@Priority(1000)
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    private static final Logger logger = LoggerFactory.getLogger(JsonParseExceptionMapper.class);
    private static final String message = "JsonParseException caught! class name: '%s', message: '%s'";

    @Override
    public Response toResponse(final JsonParseException e) {
        final ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),"Malformed JSON message."
        );

        logger.info(String.format(message, e.getClass().getName(), e.getMessage()));
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
