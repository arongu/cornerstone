package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.service.account.administration.exceptions.AccountManagerSqlBulkException;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;


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
