package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account.administration.exceptions.AccountManagerSqlBulkException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountManagerSqlBulkException> {
    @Override
    public Response toResponse(final AccountManagerSqlBulkException accountManagerSqlBulkException) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(accountManagerSqlBulkException.getExceptionMessages())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
