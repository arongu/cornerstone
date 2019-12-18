package cornerstone.workflow.app.rest.account;

import cornerstone.workflow.app.rest.rest_exceptions.BadRequestException;
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
    public Response createAccount(final AccountDTO account) throws AccountServiceException, BadRequestException {
        if ( null != account ) {
            accountService.createAccount(account.getEmail(), account.getPassword());
            return Response.status(Response.Status.CREATED)
                    .entity(account.getEmail())
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    public Response deleteAccount(final String emailAddress) throws AccountServiceException, BadRequestException {
        if ( null != emailAddress ) {
            accountService.deleteAccount(emailAddress);
            return Response.status(Response.Status.OK)
                    .entity(emailAddress)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    // /mass
    @POST
    @Path("/mass")
    public Response createAccounts(final List<AccountDTO> accounts) throws AccountServiceBulkException, BadRequestException {
        if ( null != accounts && ! accounts.isEmpty()) {
            accountService.createAccounts(accounts);
            return Response.status(Response.Status.OK)
                    .entity(accounts)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    @Path("/mass")
    public Response deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException, BadRequestException {
        if ( null != emailAddresses ) {
            accountService.deleteAccounts(emailAddresses);
            return Response.status(Response.Status.OK)
                    .entity(emailAddresses)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }
}
