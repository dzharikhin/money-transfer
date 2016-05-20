package test.money.resource;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import test.money.model.Account;
import test.money.model.Ticket;
import test.money.model.Value;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * Created by dzharikhin on 19.05.2016.
 */
public class TicketResourceTest extends AbstractMoneyRestTest {

    @Test
    public void failsToCreateTicketIfFromAccountDoesNotExist() {
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(to.getId(), to);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", UUID.randomUUID())
            .queryParam("toAccountId", to.getId())
            .request()
            .post(Entity.json(new Value(BigDecimal.valueOf(20))));
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsToCreateTicketIfToAccountDoesNotExist() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", UUID.randomUUID())
            .request()
            .post(Entity.json(new Value(BigDecimal.valueOf(20))));
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsToCreateTicketIfAccountIsNull() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .request()
            .post(Entity.json(new Value(BigDecimal.valueOf(20))));
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsToCreateTicketIfValueIsNull() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", to.getId())
            .request()
            .post(null);
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsToCreateTicketIfValueIsZero() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", to.getId())
            .request()
            .post(Entity.json(new Value(BigDecimal.ZERO)));
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsToCreateTicketIfValueIsNegative() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Response result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", to.getId())
            .request()
            .post(Entity.json(new Value(BigDecimal.valueOf(-1))));
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void successToCreateTicketIfAccountsExistsAndValueIsPositive() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Value value = new Value(BigDecimal.valueOf(10));
        Ticket result = target()
            .path("tickets")
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", to.getId())
            .request()
            .post(Entity.json(value)).readEntity(TICKET_TYPE);
        assertThat(result.getFromAccount(), is(from));
        assertThat(result.getToAccount(), is(to));
        assertThat(result.getValue(), is(value));
    }

    @Test
    public void successToFindExistingTicket() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Value value = new Value(BigDecimal.valueOf(10));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Ticket ticket = new Ticket(UUID.randomUUID(), new Date().getTime(), from, to, value);
        ticketStorage.put(ticket.getId(), ticket);
        Ticket result = target()
            .path("tickets/" + ticket.getId())
            .queryParam("fromAccountId", from.getId())
            .queryParam("toAccountId", to.getId())
            .request()
            .get().readEntity(TICKET_TYPE);
        assertThat(result, is(ticket));
    }

    @Test
    public void successToListExistingTicketsWithoutFilters() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Value value = new Value(BigDecimal.valueOf(10));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Ticket ticket1 = new Ticket(UUID.randomUUID(), new Date().getTime(), from, to, value);
        Ticket ticket2 = new Ticket(UUID.randomUUID(), new Date().getTime(), to, from, value);
        ticketStorage.put(ticket1.getId(), ticket1);
        ticketStorage.put(ticket2.getId(), ticket2);
        List<Ticket> result = target()
            .path("tickets")
            .request()
            .get().readEntity(TICKET_LIST_TYPE);
        assertThat(result, containsInAnyOrder(ticket1, ticket2));
    }

    @Test
    public void successToListExistingTicketsWithRelationFilter() {
        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(50)));
        Value value = new Value(BigDecimal.valueOf(10));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Ticket ticket1 = new Ticket(UUID.randomUUID(), new Date().getTime(), from, to, value);
        Ticket ticket2 = new Ticket(UUID.randomUUID(), new Date().getTime(), to, from, value);
        ticketStorage.put(ticket1.getId(), ticket1);
        ticketStorage.put(ticket2.getId(), ticket2);
        List<Ticket> result = target()
            .path("tickets")
            .queryParam("relatedAccountId", from.getId())
            .queryParam("relation", new Object[] {Ticket.Relation.INCOMING.name()})
            .request()
            .get().readEntity(TICKET_LIST_TYPE);
        assertThat(result, containsInAnyOrder(ticket2));
    }
}
