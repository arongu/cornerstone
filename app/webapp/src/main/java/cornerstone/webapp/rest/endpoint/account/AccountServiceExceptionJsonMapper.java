package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.service.account.administration.exceptions.AccountManagerSqlException;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceExceptionJsonMapper implements ExceptionMapper<AccountManagerSqlException> {
    @Override
    public Response toResponse(final AccountManagerSqlException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
