package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.rest.exceptions.BadRequestException;
import cornerstone.webapp.services.account_service.AccountServiceException;
import cornerstone.webapp.services.account_service.AccountServiceInterface;
import cornerstone.webapp.services.account_service.AccountServiceMultipleException;

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

    private AccountServiceInterface accountService;

    @Inject
    public AccountRestService(final AccountServiceInterface accountService) {
        this.accountService = accountService;
    }

    @POST
    public Response create(final EmailAndPassword account) throws
            AccountServiceException,
            BadRequestException {

        if ( null != account ) {
            accountService.create(
                    account.email,
                    account.password,
                    false,
                    false
            );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(account.email)
                    .build();

        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    public Response delete(final String emailAddress) throws
            AccountServiceException,
            BadRequestException {

        if ( null != emailAddress ) {
            accountService.delete(emailAddress);

            return Response.
                    status(Response.Status.OK)
                    .entity(emailAddress)
                    .build();

        } else {
            throw new BadRequestException();
        }
    }

    // /mass
    @POST
    @Path("/mass")
    public Response massCreate(final List<EmailAndPassword> accounts) throws
            AccountServiceMultipleException,
            BadRequestException {

        if ( accounts != null &&
             !accounts.isEmpty() ) {
            accountService.create(accounts);

            return Response
                    .status(Response.Status.CREATED)
                    .entity(accounts)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    @Path("/mass")
    public Response massDelete(final List<String> emailAddresses) throws
            AccountServiceMultipleException,
            BadRequestException {

        if ( null != emailAddresses ) {
            accountService.delete(emailAddresses);

            return Response
                    .status(Response.Status.OK)
                    .entity(emailAddresses)
                    .build();
        } else {
            throw new BadRequestException();
        }
    }
}
