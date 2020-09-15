package cornerstone.webapp.rest.endpoints.accounts;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSearch;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSetup;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.AccountResultSet;
import cornerstone.webapp.services.account.administration.exceptions.bulk.BulkCreationException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.BulkDeletionException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.PartialCreationException;
import cornerstone.webapp.services.account.administration.exceptions.bulk.PartialDeletionException;
import cornerstone.webapp.services.account.administration.exceptions.single.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("/accounts")
public class AccountManagerRestService {
    private static final Logger logger = LoggerFactory.getLogger(AccountManagerRestService.class);
    private final AccountManager accountManager;

    @Inject
    public AccountManagerRestService(final AccountManager accountManager) {
        this.accountManager = accountManager;
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    // to use wild cards add key%
    // or %key
    // or %key%
    @Path("search")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAddress(final AccountSearch accountSearch) {
        final String searchString = accountSearch.getSearchString();
        try {
            final List<String> results = accountManager.searchAccounts(searchString);
            if (results.size() > 0) {
                return Response.status(Response.Status.OK).entity(results).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).entity("[]").build();
            }

        } catch (final EmailAddressSearchException r) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(searchString).build();
        }
    }

    @GET
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("email") final String email) {
        try {
            final AccountResultSet accountResultSet = accountManager.get(email);
            return Response.status(Response.Status.OK).entity(accountResultSet).build();

        } catch (final NoAccountException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();

        } catch (final RetrievalException r) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(r.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final AccountEmailPassword accountEmailPassword) {
        final String email = accountEmailPassword.getEmail();
        try {
            final String password = accountEmailPassword.getPassword();
            accountManager.create(email, password, false, false);
            return Response.status(Response.Status.CREATED).entity(email).build();

        } catch (final CreationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("email") final String email) {
        try {
            accountManager.delete(email);
            return Response.status(Response.Status.OK).entity(email).build();

        } catch (final NoAccountException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();

        } catch (final DeletionException d) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(d.getMessage()).build();
        }
    }

    @POST
    @Path("bulk")
    @Produces(MediaType.APPLICATION_JSON)
    public Response massCreate(final List<AccountSetup> accountSetups) {
        try {
            accountManager.create(accountSetups);
            return Response.status(Response.Status.CREATED).entity(accountSetups).build();

        } catch (final PartialCreationException p) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(p.getExceptionMessages()).build();

        } catch (final BulkCreationException b) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(b.getMessage()).build();
        }
    }

    @DELETE
    @Path("bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response massDelete(final List<String> emailAddresses) {
        try {
            accountManager.delete(emailAddresses);
            return Response.status(Response.Status.OK).entity(emailAddresses).build();

        } catch (final PartialDeletionException p) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(p.getExceptionMessages()).build();

        } catch (final BulkDeletionException b) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(b.getMessage()).build();
        }
    }
}
