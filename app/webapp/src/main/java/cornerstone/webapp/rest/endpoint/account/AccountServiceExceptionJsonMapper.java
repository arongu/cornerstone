package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account.administration.AccountAdministrationException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceExceptionJsonMapper implements ExceptionMapper<AccountAdministrationException> {
    @Override
    public Response toResponse(final AccountAdministrationException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
