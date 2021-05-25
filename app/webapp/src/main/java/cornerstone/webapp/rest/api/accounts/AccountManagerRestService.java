package cornerstone.webapp.rest.api.accounts;

import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.rest.api.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.api.accounts.dtos.AccountSearch;
import cornerstone.webapp.rest.api.accounts.dtos.AccountSetup;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.rest.error_responses.MultiErrorResponse;
import cornerstone.webapp.services.accounts.management.AccountManager;
import cornerstone.webapp.services.accounts.management.AccountResultSet;
import cornerstone.webapp.services.accounts.management.UserRole;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.single.*;
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
//@RolesAllowed("admin")
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
    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAddress(final AccountSearch accountSearch) {
        final String searchString = accountSearch.getSearchString();
        try {
            final List<String> results = accountManager.searchAccounts(searchString);
            return Response.status(Response.Status.OK).entity(results).build();

        } catch (final AccountSearchException e) {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final AccountEmailPassword accountEmailPassword) {
        try {
            accountManager.create(
                    accountEmailPassword.getEmail().toLowerCase().trim(),
                    accountEmailPassword.getPassword().trim(),
                    false, false,
                    UserRole.USER
            );

            return Response.status(Response.Status.CREATED).header("Location", "/accounts/" + accountEmailPassword.getEmail()).build();

        } catch (final CreationException | CreationDuplicateException e) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();

        } catch (final CreationNullException e) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    e.getMessage()
            );

            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
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

        } catch (final DeletionException e2) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e2.getMessage()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @POST
    @Path("multi")
    @Produces(MediaType.APPLICATION_JSON)
    public Response massCreate(final List<AccountSetup> accountSetups) {
        try {
            accountManager.create(accountSetups);
            return Response.status(Response.Status.CREATED).build();

        } catch (final MultiCreationInitialException e2) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Database error."
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();

        } catch (final MultiCreationException e) {
            final MultiErrorResponse multiErrorResponse = new MultiErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getExceptionMessages()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(multiErrorResponse).build();
        }
    }

    @DELETE
    @Path("multi")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response massDelete(final List<String> emailAddresses) {
        try {
            accountManager.delete(emailAddresses);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (final MultiDeletionInitialException e2) {
            final ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Database error."
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();

        } catch (final MultiDeletionException e) {
            final MultiErrorResponse multiErrorResponse = new MultiErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getExceptionMessages()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(multiErrorResponse).build();
        }
    }
}
