package cornerstone.workflow.app.rest.account;

import cornerstone.workflow.app.rest.rest_exceptions.BadRequestException;
import cornerstone.workflow.app.services.account_service.AccountCrudService;
import cornerstone.workflow.app.services.account_service.AccountCrudServiceBulkException;
import cornerstone.workflow.app.services.account_service.AccountCrudServiceException;

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
    private AccountCrudService accountCrudService;

    @Inject
    public AccountRestService(final AccountCrudService accountCrudService) {
        this.accountCrudService = accountCrudService;
    }

    @POST
    public Response createAccount(final AccountLoginJsonDto account) throws AccountCrudServiceException, BadRequestException {
        if ( null != account ) {
            accountCrudService.createAccount(account.getEmail(), account.getPassword(), true);
            return Response.status(Response.Status.CREATED)
                    .entity(account.getEmail())
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    public Response deleteAccount(final String emailAddress) throws AccountCrudServiceException, BadRequestException {
        if ( null != emailAddress ) {
            accountCrudService.deleteAccount(emailAddress);
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
    public Response createAccounts(final List<AccountLoginJsonDto> accounts) throws AccountCrudServiceBulkException, BadRequestException {
        if ( null != accounts && ! accounts.isEmpty()) {
            accountCrudService.createAccounts(accounts);
            return Response.status(Response.Status.OK)
                    .entity(accounts)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    @Path("/mass")
    public Response deleteAccounts(final List<String> emailAddresses) throws AccountCrudServiceBulkException, BadRequestException {
        if ( null != emailAddresses ) {
            accountCrudService.deleteAccounts(emailAddresses);
            return Response.status(Response.Status.OK)
                    .entity(emailAddresses)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }
}
