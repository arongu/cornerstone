package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account.administration.AccountAdministrationException;
import cornerstone.webapp.services.account.administration.AccountAdministrationMultipleException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountAdministrationMultipleException> {
    @Override
    public Response toResponse(final AccountAdministrationMultipleException accountAdministrationMultipleException) {
        final List<String> exceptionMessages = new LinkedList<>();

        for (AccountAdministrationException e : accountAdministrationMultipleException.getExceptions()){
            exceptionMessages.add(e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionMessages)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
