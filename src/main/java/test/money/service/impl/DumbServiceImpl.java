package test.money.service.impl;

import test.money.exception.AccountException;
import test.money.exception.TicketException;
import test.money.model.Account;
import test.money.model.Ticket;
import test.money.model.Value;
import test.money.service.AccountService;
import test.money.service.TicketService;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public class DumbServiceImpl implements AccountService, TicketService {

    private static final Logger LOG = Logger.getLogger(DumbServiceImpl.class.getName());

    private final ConcurrentMap<UUID, Account> accounts;
    private final ConcurrentMap<UUID, Ticket> tickets;
    private final ConcurrentMap<UUID, ReentrantLock> locks;
    private final Executor executorService;

    private final long jobEmulationDuration;

    public DumbServiceImpl(
        ConcurrentMap<UUID, Account> accounts,
        ConcurrentMap<UUID, Ticket> tickets,
        Executor executor,
        long jobEmulationDuration
    ) {
        this.accounts = accounts;
        this.tickets = tickets;
        this.locks = new ConcurrentHashMap<>();
        this.executorService = executor;
        this.jobEmulationDuration = jobEmulationDuration;
    }


    @Override
    public Ticket createTicket(
        @NotNull Account fromAccount,
        @NotNull Account toAccount,
        @NotNull Value value
    ) {
        Ticket result = new Ticket(UUID.randomUUID(), new Date().getTime(), fromAccount, toAccount, value);
        validateTicketSettings(result);
        try {
            LOG.finest(format("Saving ticket with id={0} to datastore", result.getId()));
            Ticket previous = tickets.putIfAbsent(result.getId(), result);
            if (previous != null) {
                LOG.warning(format("Ticket with id={0} already exists", result.getId()));
                throw new TicketException(result);
            }
            LOG.finest(format("Setting ticket with id={0} to process queue", result.getId()));
            executorService.execute(new TicketProcessingTask(result));
            return result;
        } catch (RejectedExecutionException e) {
            LOG.warning(format("Ticket with id={0} failed to process with exception: {0}", result.getId(), e.getMessage()));
            result.setStatus(Ticket.Status.FAILED);
            throw new TicketException(result);
        }
    }

    private void validateTicketSettings(Ticket result) {
        if (result.getValue().getSum().compareTo(BigDecimal.ZERO) <= 0) {
            LOG.info(format("Value of ticet with id={0} <= 0", result.getId()));
            throw new TicketException(result);
        }
    }

    @Override
    public Collection<Ticket> getTickets(
        Account relatedAccount, Collection<Ticket.Relation> relationFilter
    ) {
        return tickets.values().stream().filter((ticket) ->
            relatedAccount == null
            || relationFilter == null || relationFilter.isEmpty()
            || (relationFilter.contains(Ticket.Relation.OUTCOMING) && ticket.getFromAccount().equals(relatedAccount))
            || (relationFilter.contains(Ticket.Relation.INCOMING) && ticket.getToAccount().equals(relatedAccount)))
        .collect(Collectors.toList());
    }

    @Override
    public Optional<Ticket> getTicket(@NotNull UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    @Override
    public Ticket updateTicket(@NotNull Ticket ticket, Ticket.Status status) {
        ticket.setStatus(status);
        return ticket;
    }

    @Override
    public Account createAccount(@NotNull UUID id, String label) {
        Account result = new Account(id, label, new Value(BigDecimal.ZERO));
        Account previous = accounts.putIfAbsent(id, result);
        if (previous != null) {
            LOG.warning(format("Account with id={0} already exists", result.getId()));
            throw new AccountException(result);
        }
        return result;
    }

    @Override
    public Collection<Account> getAccounts(Collection<String> labelFilter) {
        return accounts.values().stream().filter(account ->
            labelFilter == null
            || labelFilter.isEmpty()
            || labelFilter.contains(account.getLabel())
        ).collect(Collectors.toList());
    }

    @Override
    public Optional<Account> getAccount(@NotNull UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account updateAccount(@NotNull Account account, String label) {
        account.setLabel(label);
        return account;
    }

    @Override
    public void deleteAccount(@NotNull Account account) {
        accounts.remove(account.getId());
        LOG.info(format("Account with id={0} was removed", account.getId()));
    }

    private ReentrantLock getLock(Account account) {
        return locks.computeIfAbsent(account.getId(), (id) -> new ReentrantLock());
    }

    //for testing
    List<ReentrantLock> getSortedLocks(Account fromAccount, Account toAccount) {
        return Stream.<Account>builder().add(fromAccount)
            .add(toAccount)
            .build()
            .sorted()
            .map(DumbServiceImpl.this::getLock)
            .collect(Collectors.toList());
    }

    private class TicketProcessingTask implements Runnable {

        private final Ticket ticket;

        private TicketProcessingTask(Ticket ticket) {
            this.ticket = ticket;
        }

        @Override
        public void run() {
            try {
                updateTicket(ticket, Ticket.Status.PROCESSING);
                getSortedLocks(ticket.getFromAccount(), ticket.getToAccount()).forEach(ReentrantLock::lock);
                //work emulation
                Thread.sleep(jobEmulationDuration);
                Account fromAcc = validateAndGetFromAccount(ticket.getValue());
                Account toAcc = getAccount(ticket.getToAccount().getId())
                    .orElseThrow(() -> new TicketException(ticket));
                fromAcc.getValue().setSum(fromAcc.getValue().getSum().subtract(ticket.getValue().getSum()));
                toAcc.getValue().setSum(toAcc.getValue().getSum().add(ticket.getValue().getSum()));
                updateTicket(ticket, Ticket.Status.COMPLETE);
            } catch (TicketException | InterruptedException e) {
                updateTicket(ticket, Ticket.Status.FAILED);
            } finally {
                getSortedLocks(ticket.getFromAccount(), ticket.getToAccount()).forEach(ReentrantLock::unlock);
            }
        }

        private Account validateAndGetFromAccount(Value value) {
            Account fromAcc = getAccount(ticket.getFromAccount().getId())
                .orElseThrow(() -> new TicketException(ticket));
            if (fromAcc.getValue().getSum().compareTo(value.getSum()) < 0) {
                throw new TicketException(ticket);
            }
            return fromAcc;
        }
    }
}
