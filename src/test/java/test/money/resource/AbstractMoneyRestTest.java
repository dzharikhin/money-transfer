package test.money.resource;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import test.money.App;
import test.money.model.Account;
import test.money.model.Ticket;
import test.money.service.impl.DumbServiceImpl;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.glassfish.jersey.client.ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION;

/**
 * Created by dzharikhin on 19.05.2016.
 */
public abstract class AbstractMoneyRestTest extends JerseyTest {

    private static final int port = 10;

    protected static final GenericType<Account> ACCOUNT_TYPE = new GenericType<Account>() {};
    protected static final GenericType<List<Account>> ACCOUNT_LIST_TYPE = new GenericType<List<Account>>() {};
    protected static final GenericType<Ticket> TICKET_TYPE = new GenericType<Ticket>() {};
    protected static final GenericType<List<Ticket>> TICKET_LIST_TYPE = new GenericType<List<Ticket>>() {};

    protected ConcurrentMap<UUID, Account> accountStorage;
    protected ConcurrentMap<UUID, Ticket> ticketStorage;

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri("http://localhost/").port(port).build();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, String.valueOf(port));
        accountStorage = new ConcurrentHashMap<>();
        ticketStorage = new ConcurrentHashMap<>();
        ResourceConfig resourceConfig = App.create(new DumbServiceImpl(accountStorage, ticketStorage, Runnable::run, 0));
        configure(resourceConfig);
        return resourceConfig;
    }

    @Override
    protected Client getClient() {
        Client client = super.getClient();
        client.property(SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        return client;
    }

    protected void configure(ResourceConfig resourceConfig) {

    }
}
