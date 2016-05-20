package test.money.resource;

import test.money.util.WebUtil;
import test.money.model.Ticket;
import test.money.model.Value;
import test.money.service.AccountService;
import test.money.service.TicketService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by dzharikhin on 17.05.2016.
 */
@Path("tickets")
public class TicketResourse {

    @Inject
    private AccountService accountService;

    @Inject
    private TicketService ticketService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket createTicket(
        @NotNull @QueryParam("fromAccountId") UUID fromAccountId,
        @NotNull @QueryParam("toAccountId") UUID toAccountId,
        @NotNull Value value
    ) {
        return ticketService.createTicket(
            WebUtil.unwrapWithBadRequstException(accountService.getAccount(fromAccountId)),
            WebUtil.unwrapWithBadRequstException(accountService.getAccount(toAccountId)),
            value
        );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Ticket> getTickets(
        @QueryParam("relatedAccountId") UUID relatedAccountId,
        @DefaultValue("OUTCOMING") @QueryParam("relation") List<Ticket.Relation> relations
    ) {
        return ticketService.getTickets(
            relatedAccountId != null ? WebUtil.unwrapWithBadRequstException(accountService.getAccount(relatedAccountId)) : null,
            relations
        );
    }

    @GET
    @Path("{ticketId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket getTicket(@PathParam("ticketId") UUID ticketId) {
        return WebUtil.unwrapWithNotFoundException(ticketService.getTicket(ticketId));
    }

}
