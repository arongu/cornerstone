package cornerstone.workflow.restapi.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionMapperImpl implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(final Exception e) {
        if ( e instanceof Exception){
            return Response.status(500)
                    .entity(new ExceptionMessage(500, e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        return  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
