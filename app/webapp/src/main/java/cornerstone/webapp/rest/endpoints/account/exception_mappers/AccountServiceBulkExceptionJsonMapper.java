package cornerstone.webapp.rest.endpoints.account.exception_mappers;

import cornerstone.webapp.services.account.administration.exceptions.bulk.PartialCreationException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<PartialCreationException> {
    @Override
    public Response toResponse(final PartialCreationException partialCreationException) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(partialCreationException.getExceptionMessages())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
