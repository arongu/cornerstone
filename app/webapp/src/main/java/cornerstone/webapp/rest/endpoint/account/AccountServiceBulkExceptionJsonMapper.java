package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.services.account.administration.AccountManagerSqlException;
import cornerstone.webapp.services.account.administration.AccountManagerSqlBulkException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

@Singleton
@Provider
public class AccountServiceBulkExceptionJsonMapper implements ExceptionMapper<AccountManagerSqlBulkException> {
    @Override
    public Response toResponse(final AccountManagerSqlBulkException accountManagerSqlBulkException) {
        final List<String> exceptionMessages = new LinkedList<>();

        for (AccountManagerSqlException e : accountManagerSqlBulkException.getExceptionMessages()){
            exceptionMessages.add(e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionMessages)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
