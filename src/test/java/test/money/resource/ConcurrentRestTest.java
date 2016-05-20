package test.money.resource;

import org.junit.Ignore;
import org.junit.Test;
import test.money.model.Account;
import test.money.model.Ticket;
import test.money.model.Value;

import javax.ws.rs.client.Entity;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by dzharikhin (https://github.com/dzharikhin) on 20.05.2016.
 */
//NOT A UNIT TEST - FALSE POSITIVE RESULT IS POSSIBLE
@Ignore
public class ConcurrentRestTest extends AbstractMoneyRestTest {

    @Test
    public void testConcurrency() {
        int threadCount = 8;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Ticket> tickets = new CopyOnWriteArrayList<>();

        Account from = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(10)));
        Account to = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.valueOf(10)));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Value value = new Value(BigDecimal.valueOf(10));

        IntStream.range(0, threadCount).forEach((num) -> {
            try {
                Thread thread = new Thread(() -> {
                    latch.countDown();
                    tickets.add(target()
                        .path("tickets")
                        .queryParam("fromAccountId", from.getId())
                        .queryParam("toAccountId", to.getId())
                        .request()
                        .post(Entity.json(value)).readEntity(TICKET_TYPE)
                    );
                });
                thread.start();
                thread.join();
            } catch (InterruptedException ignore) {
            }
        });
        assertThat(tickets.stream().filter(t -> t.getStatus() == Ticket.Status.COMPLETE).count(), is(1L));
    }
}
