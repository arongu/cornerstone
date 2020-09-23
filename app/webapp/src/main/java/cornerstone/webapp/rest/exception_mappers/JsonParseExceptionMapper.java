package cornerstone.webapp.rest.exception_mappers;

import com.fasterxml.jackson.core.JsonParseException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    @Override
    public Response toResponse(JsonParseException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(":(")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
