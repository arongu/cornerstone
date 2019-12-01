package cornerstone.workflow.app.rest.exception_mappers;

import cornerstone.workflow.app.services.account_service.AccountServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AccountServiceExceptionMapper implements ExceptionMapper<AccountServiceException> {
    @Override
    public Response toResponse(AccountServiceException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
