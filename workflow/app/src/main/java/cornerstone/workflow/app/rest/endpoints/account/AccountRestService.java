package cornerstone.workflow.app.rest.endpoints.account;

import cornerstone.workflow.app.rest.RestMessageTemplate;
import cornerstone.workflow.app.services.account_service.AccountService;
import cornerstone.workflow.app.services.account_service.AccountServiceBulkException;
import cornerstone.workflow.app.services.account_service.AccountServiceException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;

@Provider
@Singleton
@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {
    private AccountService accountService;

    @Inject
    public AccountRestService(final AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    public Response createAccount(final AccountDTO account) throws AccountServiceException {
        accountService.createAccount(account.getEmail(), account.getPassword());
        return Response.status(Response.Status.CREATED)
                .entity(account.getEmail())
                .build();
    }

    @DELETE
    public Response deleteAccount(final String emailAddress) throws AccountServiceException {
        accountService.deleteAccount(emailAddress);
        return Response.status(Response.Status.OK)
                .entity(emailAddress)
                .build();
    }

    // /mass
    @POST
    @Path("/mass")
    public Response createAccounts(final List<AccountDTO> accounts) throws AccountServiceBulkException {
        if ( null != accounts && ! accounts.isEmpty()) {
            accountService.createAccounts(accounts);
            return Response.status(Response.Status.OK).entity(accounts).build();
        } else {
            final String badRequest = RestMessageTemplate.getHttpStatusMessageAsJSON("BAD_REQUEST", Response.Status.BAD_REQUEST.getStatusCode());
            return Response.status(Response.Status.BAD_REQUEST).entity(badRequest).build();
        }
    }

    @DELETE
    @Path("/mass")
    public Response deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException {
        accountService.deleteAccounts(emailAddresses);
        return Response.status(Response.Status.OK)
                .entity(emailAddresses)
                .build();
    }
}
