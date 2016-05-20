package test.money.resource;

import test.money.util.WebUtil;
import test.money.model.Account;
import test.money.service.AccountService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by dzharikhin on 17.05.2016.
 */
@Path("accounts")
public class AccountResource {

    @Inject
    private AccountService accountService;

    @POST
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account createAccount(
        @PathParam("accountId") UUID id,
        @DefaultValue("account") @QueryParam("label") String label
    ) {
        return accountService.createAccount(id, label);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Account> getAccounts(
        @QueryParam("labelFilter") List<String> labelFilter
    ) {
        return accountService.getAccounts(labelFilter);
    }

    @GET
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountId") UUID accountId) {
        return WebUtil.unwrapWithNotFoundException(accountService.getAccount(accountId));
    }

    @PUT
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account updateAccount(
        @PathParam("accountId") UUID accountId,
        @QueryParam("label") String label
    ) {
        return accountService.updateAccount(
            WebUtil.unwrapWithBadRequstException(accountService.getAccount(accountId)),
            label
        );
    }

    @DELETE
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(@PathParam("accountId") UUID accountId) {
        accountService.deleteAccount(
            WebUtil.unwrapWithBadRequstException(accountService.getAccount(accountId))
        );
        return Response.ok().build();
    }
}
