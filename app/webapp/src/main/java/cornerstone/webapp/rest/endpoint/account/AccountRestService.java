package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.service.account.administration.AccountManager;
import cornerstone.webapp.service.account.administration.AccountResultSet;
import cornerstone.webapp.service.account.administration.exceptions.AccountDoesNotExistException;
import cornerstone.webapp.service.account.administration.exceptions.AccountManagerBulkException;
import cornerstone.webapp.service.account.administration.exceptions.AccountManagerSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("/account")
public class AccountRestService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRestService.class);
    private final AccountManager accountManager;

    @Inject
    public AccountRestService(final AccountManager accountManager) {
        this.accountManager = accountManager;
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }


    @GET
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("email") final String email) throws AccountManagerSqlException {
        try {
            final AccountResultSet accountResultSet = accountManager.get(email);
            return Response.status(Response.Status.OK).entity(accountResultSet).build();
        } catch (final AccountDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(email).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final AccountEmailPassword accountEmailPassword) throws AccountManagerSqlException {
        final String email = accountEmailPassword.getEmail();
        final String password = accountEmailPassword.getPassword();

        accountManager.create(email, password, false, false);
        return Response.status(Response.Status.CREATED).entity("todo").build();
    }

    @DELETE
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("email") final String email) throws AccountManagerSqlException {
        final int n = accountManager.delete(email);
        if (n > 0) {
            return Response.status(Response.Status.OK).entity(email).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(email).build();
        }
    }

    @POST
    @Path("bulk")
    @Produces(MediaType.APPLICATION_JSON)
    public Response massCreate(final List<AccountEmailPassword> accounts) throws AccountManagerSqlException, AccountManagerBulkException {
        accountManager.create(accounts);
        return Response.status(Response.Status.CREATED).entity(accounts).build();
    }

    @DELETE
    @Path("bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response massDelete(final List<String> emailAddresses) throws AccountManagerSqlException, AccountManagerBulkException {
        accountManager.delete(emailAddresses);
        return Response.status(Response.Status.OK).entity(emailAddresses).build();
    }
}
