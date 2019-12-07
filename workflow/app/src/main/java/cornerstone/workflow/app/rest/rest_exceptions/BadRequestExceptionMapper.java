package cornerstone.workflow.app.rest.rest_exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    private static final Logger logger = LoggerFactory.getLogger(BadRequestExceptionMapper.class);

    public BadRequestExceptionMapper() {
        logger.info("BadRequestExceptionMapper BadRequestExceptionMapper BadRequestExceptionMapper BadRequestExceptionMapper  CREATED");
    }

    //.entity(new HttpMessage("BAD_REQUEST", Response.Status.BAD_REQUEST.getStatusCode()))
    @Override
    public Response toResponse(final BadRequestException e) {
        logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("BAD_REQUEST")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
