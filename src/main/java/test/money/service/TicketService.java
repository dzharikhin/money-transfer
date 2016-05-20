package test.money.service;

import test.money.model.Account;
import test.money.model.Ticket;
import test.money.model.Value;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public interface TicketService {

    Ticket createTicket(@NotNull Account fromAccount, @NotNull Account toAccount, @NotNull Value value);
    Collection<Ticket> getTickets(Account relatedAccountId, Collection<Ticket.Relation> relationFilter);
    Optional<Ticket> getTicket(@NotNull UUID id);
    Ticket updateTicket(@NotNull Ticket ticket, @NotNull Ticket.Status status);
}
