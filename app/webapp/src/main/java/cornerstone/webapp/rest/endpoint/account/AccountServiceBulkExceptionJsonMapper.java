package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.service.account.administration.exceptions.AccountBulkCreationException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountBulkCreationException> {
    @Override
    public Response toResponse(final AccountBulkCreationException accountBulkCreationException) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(accountBulkCreationException.getExceptionMessages())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
