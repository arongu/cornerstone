package cornerstone.workflow.app.rest.endpoints.account;

import cornerstone.workflow.app.services.account_service.AccountService;
import cornerstone.workflow.app.services.account_service.AccountServiceBulkException;
import cornerstone.workflow.app.services.account_service.AccountServiceException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("/account")
public class AccountRestService {
    private AccountService accountService;

    @Inject
    public AccountRestService(final AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(final AccountDTO account) throws AccountServiceException {
        accountService.createAccount(account.getEmail(), account.getPassword());
        return Response.status(Response.Status.CREATED)
                .entity(account.getEmail())
                .build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(final String emailAddress) throws AccountServiceException {
        accountService.deleteAccount(emailAddress);
        return Response.status(Response.Status.OK)
                .entity(emailAddress)
                .build();
    }

    // /MASS
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/mass")
    public Response createAccounts(final List<AccountDTO> accounts) throws AccountServiceBulkException {
        accountService.createAccounts(accounts);
        return Response.status(Response.Status.OK)
                .entity(accounts)
                .build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/mass")
    public Response deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException {
        accountService.deleteAccounts(emailAddresses);
        return Response.status(Response.Status.OK)
                .entity(emailAddresses)
                .build();
    }
}
