package cornerstone.workflow.app.rest.endpoint.admin;

import cornerstone.workflow.app.services.admin.AccountManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Singleton
@Path("/admin")
public class AdminRestService {
    private AccountManager accountManager;

    @Inject
    public AdminRestService(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(final AccountDTO accountDTO){
        accountManager.createAccount(accountDTO.getEmail(), accountDTO.getPassword());
        return Response.status(244).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(final String  email){
        accountManager.deleteAccount(email);
        return Response.status(244).build();
    }

    // BULK SERVICE
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bulk")
    public Response createAccounts(final List<AccountDTO> accountDTOS) throws SQLException {
        accountManager.createAccounts(accountDTOS);
        return Response.status(244).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bulk")
    public Response deleteAccounts(final List<String> emails){
        accountManager.deleteAccounts(emails);
        return Response.status(244).build();
    }
}
