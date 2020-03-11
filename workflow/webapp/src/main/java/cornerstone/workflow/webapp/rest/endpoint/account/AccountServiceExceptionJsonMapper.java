package cornerstone.workflow.webapp.rest.endpoint.account;

import cornerstone.workflow.webapp.services.account_service.AccountServiceException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class AccountServiceExceptionJsonMapper implements ExceptionMapper<AccountServiceException> {
    @Override
    public Response toResponse(final AccountServiceException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
