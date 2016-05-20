package test.money;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import test.money.model.Account;
import test.money.model.Value;
import test.money.service.AccountService;
import test.money.service.TicketService;
import test.money.service.impl.DumbServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public class App {
    private static final URI BASE_URI = URI.create("http://localhost:8080/v1/");

    public static void main(String[] args) throws IOException {
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(setUpService()));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            server.shutdown();
        }
    }

    public static ResourceConfig create(DumbServiceImpl service) {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages("test.money");
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(service).to(AccountService.class);
                bind(service).to(TicketService.class);
            }
        });
        resourceConfig.register(ValidationFeature.class);
        return resourceConfig;
    }

    public static DumbServiceImpl setUpService() {
        ConcurrentMap<UUID, Account> accountStorage = new ConcurrentHashMap<>();
        Account acc1 = new Account(UUID.randomUUID(), "account1", new Value(BigDecimal.valueOf(200)));
        accountStorage.put(acc1.getId(), acc1);
        return new DumbServiceImpl(
            accountStorage,
            new ConcurrentHashMap<>(),
            Executors.newCachedThreadPool(),
            TimeUnit.SECONDS.toMillis(2)
        );
    }
}
