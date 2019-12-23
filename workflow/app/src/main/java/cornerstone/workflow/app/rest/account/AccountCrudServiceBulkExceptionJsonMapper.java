package cornerstone.workflow.app.rest.account;

import cornerstone.workflow.app.services.account_service.AccountCrudServiceBulkException;
import cornerstone.workflow.app.services.account_service.AccountCrudServiceException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

@Singleton
@Provider
public class AccountCrudServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountCrudServiceBulkException> {
    @Override
    public Response toResponse(final AccountCrudServiceBulkException accountCrudServiceBulkException) {
        final List<String> exceptionMessages = new LinkedList<>();

        for (AccountCrudServiceException e : accountCrudServiceBulkException.getExceptions()){
            exceptionMessages.add(e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionMessages)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
