package cornerstone.webapp.rest.endpoints.accounts;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSearch;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSetup;
import cornerstone.webapp.rest.error_responses.MultiErrorResponse;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.AccountResultSet;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.account.administration.exceptions.multi.MultiDeletionException;
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
                return Response.status(Response.Status.OK).entity("[]").build();
            }

        } catch (final EmailAddressSearchException e) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
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
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();

        } catch (final RetrievalException r) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    r.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final AccountEmailPassword accountEmailPassword) {
        try {
            accountManager.create(
                    accountEmailPassword.getEmail(),
                    accountEmailPassword.getPassword(),
                    false, false
            );

            return Response.status(Response.Status.CREATED).build();

        } catch (final CreationException e) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @DELETE
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("email") final String email) {
        try {
            accountManager.delete(email);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (final NoAccountException e) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();

        } catch (final DeletionException d) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    d.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @POST
    @Path("bulk")
    @Produces(MediaType.APPLICATION_JSON)
    public Response massCreate(final List<AccountSetup> accountSetups) {
        try {
            accountManager.create(accountSetups);
            return Response.status(Response.Status.CREATED).build();

        } catch (final MultiCreationException p) {
            final MultiErrorResponse multiErrorResponse = new MultiErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    p.getExceptionMessages()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(multiErrorResponse).build();

        } catch (final MultiCreationInitialException b) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Database error."
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @DELETE
    @Path("bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response massDelete(final List<String> emailAddresses) {
        try {
            accountManager.delete(emailAddresses);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (final MultiDeletionException p) {
            final MultiErrorResponse multiErrorResponse = new MultiErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    p.getExceptionMessages()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(multiErrorResponse).build();

        } catch (final MultiDeletionInitialException b) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Database error."
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }
}
