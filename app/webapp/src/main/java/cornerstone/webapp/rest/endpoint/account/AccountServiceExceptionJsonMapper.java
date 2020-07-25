package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account.administration.AccountManagerException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceExceptionJsonMapper implements ExceptionMapper<AccountManagerException> {
    @Override
    public Response toResponse(final AccountManagerException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
