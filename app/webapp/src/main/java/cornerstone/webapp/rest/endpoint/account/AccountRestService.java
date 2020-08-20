package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.service.account.administration.AccountManager;
import cornerstone.webapp.service.account.administration.AccountResultSet;
import cornerstone.webapp.service.account.administration.exceptions.AccountDoesNotExistException;
import cornerstone.webapp.service.account.administration.exceptions.AccountManagerSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        logger.info(".......................... " + email);
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
    public Response create(AccountEmailPassword accountEmailPassword) throws AccountManagerSqlException {
        logger.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        final String email = accountEmailPassword.getEmail();
        final String password = accountEmailPassword.getPassword();

        accountManager.create(email, password, false, false);
        return Response.status(Response.Status.CREATED).entity(email).build();
    }

//    @DELETE
//    @Path("{email}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response delete(@PathParam("email") final String email) throws AccountManagerSqlException {
//        final int n = accountManager.delete(email);
//        if (n > 0) {
//            return Response.status(Response.Status.OK).entity(email).build();
//        } else {
//            return Response.status(Response.Status.NOT_FOUND).entity(email).build();
//        }
//    }
//
//    // /mass
//    @POST
//    @Path("/mass")
//    public Response massCreate(final List<AccountEmailPassword> accounts) throws AccountManagerSqlException, AccountManagerSqlBulkException {
//        accountManager.create(accounts);
//        return Response.status(Response.Status.CREATED).entity(accounts).build();
//    }
//
//    @DELETE
//    @Path("/mass")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response massDelete(final List<String> emailAddresses) throws AccountManagerSqlException, AccountManagerSqlBulkException {
//        accountManager.delete(emailAddresses);
//        return Response.status(Response.Status.OK).entity(emailAddresses).build();
//    }
}
