package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account_service.AccountServiceException;
import cornerstone.webapp.services.account_service.AccountServiceMultipleException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountServiceMultipleException> {
    @Override
    public Response toResponse(final AccountServiceMultipleException accountServiceMultipleException) {
        final List<String> exceptionMessages = new LinkedList<>();

        for (AccountServiceException e : accountServiceMultipleException.getExceptions()){
            exceptionMessages.add(e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionMessages)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
