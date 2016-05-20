package test.money.service.impl;

import org.junit.Before;
import org.junit.Test;
import test.money.exception.TicketException;
import test.money.model.Account;
import test.money.model.Ticket;
import test.money.model.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by dzharikhin on 19.05.2016.
 */
public class ProcessingTest {
    DumbServiceImpl testedService;
    ConcurrentMap<UUID, Account> accountStorage;

    @Before
    public void setUp() throws Exception {
        accountStorage = new ConcurrentHashMap<>();
        testedService = new DumbServiceImpl(
            accountStorage,
            new ConcurrentHashMap<>(),
            Runnable::run,
            100);
    }

    @Test
    public void orderOfLocksIsTheSame() {
        Account acc1 = new Account(UUID.randomUUID(), "acc1", new Value(BigDecimal.ZERO));
        Account acc2 = new Account(UUID.randomUUID(), "acc2", new Value(BigDecimal.ZERO));
        assertEquals(testedService.getSortedLocks(acc1, acc2), testedService.getSortedLocks(acc2, acc1));
    }

    @Test
    public void successProcessingIfSufficientBalanceOnFromAccount() {
        Account from = new Account(UUID.randomUUID(), "from", new Value(BigDecimal.valueOf(10)));
        Account to = new Account(UUID.randomUUID(), "to", new Value(BigDecimal.ZERO));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Value value = new Value(BigDecimal.valueOf(10));
        Ticket ticket = testedService.createTicket(from, to, value);
        assertThat(ticket.getStatus(), is(Ticket.Status.COMPLETE));
    }

    @Test
    public void failsProcessingIfNotSufficientBalanceOnFromAccount() {
        Account from = new Account(UUID.randomUUID(), "from", new Value(BigDecimal.valueOf(9)));
        Account to = new Account(UUID.randomUUID(), "to", new Value(BigDecimal.ZERO));
        accountStorage.put(from.getId(), from);
        accountStorage.put(to.getId(), to);
        Value value = new Value(BigDecimal.valueOf(10));
        Ticket ticket = testedService.createTicket(from, to, value);
        assertThat(ticket.getStatus(), is(Ticket.Status.FAILED));
    }

    @Test
    public void failsProcessingIfNotSufficientResourcesToProcess() throws ExecutionException, InterruptedException {
        int totalResources = 2;
        int availableResourcesCount = 1;
        AtomicInteger usedResourcesCount = new AtomicInteger(0);
        List<Ticket> results = new ArrayList<>();
        List<TicketException> exceptions = new ArrayList<>();
        Value value = new Value(BigDecimal.valueOf(10));

        testedService = new DumbServiceImpl(
            accountStorage,
            new ConcurrentHashMap<>(),
            runnable -> {
                if (usedResourcesCount.incrementAndGet() > availableResourcesCount) {
                    throw new RejectedExecutionException();
                } else {
                    runnable.run();
                }
            },
            0
        );
        IntStream.range(0, totalResources).forEach(num -> {
            Account from = new Account(UUID.randomUUID(), "from" + num, new Value(BigDecimal.valueOf(10)));
            Account to = new Account(UUID.randomUUID(), "to" + num, new Value(BigDecimal.ZERO));
            accountStorage.put(from.getId(), from);
            accountStorage.put(to.getId(), to);
            try {
                results.add(testedService.createTicket(from, to, value));

            } catch (TicketException e) {
                exceptions.add(e);
            }
        });
        assertThat(results.size(), is(availableResourcesCount));
        assertThat(exceptions.size(), is(totalResources - availableResourcesCount));
    }
}
