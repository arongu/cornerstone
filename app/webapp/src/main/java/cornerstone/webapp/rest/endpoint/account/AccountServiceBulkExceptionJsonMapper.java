package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.service.account.administration.exceptions.AccountManagerBulkException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountManagerBulkException> {
    @Override
    public Response toResponse(final AccountManagerBulkException accountManagerBulkException) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(accountManagerBulkException.getExceptionMessages())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
