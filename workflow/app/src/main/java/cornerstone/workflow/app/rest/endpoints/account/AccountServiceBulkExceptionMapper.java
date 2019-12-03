package cornerstone.workflow.app.rest.endpoints.account;

import cornerstone.workflow.app.services.account_service.AccountServiceBulkException;
import cornerstone.workflow.app.services.account_service.AccountServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

@Provider
public class AccountServiceBulkExceptionMapper implements ExceptionMapper<AccountServiceBulkException> {
    @Override
    public Response toResponse(final AccountServiceBulkException bulkException) {
        final List<String> exceptionMessages = new LinkedList<>();
        for (AccountServiceException e : bulkException.getExceptions()){
            exceptionMessages.add(e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionMessages)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
