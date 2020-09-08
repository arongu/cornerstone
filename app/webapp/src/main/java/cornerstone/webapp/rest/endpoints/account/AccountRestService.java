package cornerstone.webapp.rest.endpoints.account;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.rest.endpoints.account.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.endpoints.account.dtos.AccountSetup;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.AccountResultSet;
import cornerstone.webapp.services.account.administration.exceptions.bulk.PartialCreationException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.BulkCreationException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.PartialDeletionException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.BulkDeletionException;
import cornerstone.webapp.services.account.administration.exceptions.single.CreationException;
import cornerstone.webapp.services.account.administration.exceptions.single.DeletionException;
import cornerstone.webapp.services.account.administration.exceptions.single.NoAccountException;
import cornerstone.webapp.services.account.administration.exceptions.single.RetrievalException;
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
    public Response getAccount(@PathParam("email") final String email) throws RetrievalException {
        try {
            final AccountResultSet accountResultSet = accountManager.get(email);
            return Response.status(Response.Status.OK).entity(accountResultSet).build();
        } catch (final NoAccountException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(email).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final AccountEmailPassword accountEmailPassword) throws CreationException {
        final String email = accountEmailPassword.getEmail();
        final String password = accountEmailPassword.getPassword();

        accountManager.create(email, password, false, false);
        return Response.status(Response.Status.CREATED).entity("todo").build();
    }

    @DELETE
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("email") final String email) throws DeletionException, NoAccountException {
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
    public Response massCreate(final List<AccountSetup> accountSetups) throws PartialCreationException, BulkCreationException {
        accountManager.create(accountSetups);
        return Response.status(Response.Status.CREATED).entity(accountSetups).build();
    }

    @DELETE
    @Path("bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response massDelete(final List<String> emailAddresses) throws PartialDeletionException, BulkDeletionException {
        accountManager.delete(emailAddresses);
        return Response.status(Response.Status.OK).entity(emailAddresses).build();
    }
}
