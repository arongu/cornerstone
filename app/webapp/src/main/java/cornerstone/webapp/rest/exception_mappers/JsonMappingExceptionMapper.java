package cornerstone.webapp.rest.exception_mappers;

import com.fasterxml.jackson.databind.JsonMappingException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {
    @Override
    public Response toResponse(JsonMappingException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(":(")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
