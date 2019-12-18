package cornerstone.workflow.app.rest.rest_exceptions;

import cornerstone.workflow.app.rest.rest_messages.HttpMessage;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    public BadRequestExceptionMapper() {
    }

    @Override
    public Response toResponse(final BadRequestException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new HttpMessage(Response.Status.BAD_REQUEST.toString(), Response.Status.BAD_REQUEST.getStatusCode()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
