package test.money.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public class Ticket {

    public enum Relation {
        OUTCOMING,
        INCOMING
    }

    public enum Status {
        QUEUED,
        PROCESSING,
        COMPLETE,
        FAILED
    }

    private UUID id;
    private long timestamp;
    private Account fromAccount;
    private Account toAccount;
    private Value value;
    private Status status;

    public Ticket(UUID id, long timestamp, Account fromAccount, Account toAccount, Value value) {
        this.status = Status.QUEUED;
        this.id = id;
        this.timestamp = timestamp;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.value = value;
    }

    //jackson
    public Ticket() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ticket{" +
            "id=" + id +
            ", timestamp=" + timestamp +
            ", fromAccount=" + fromAccount +
            ", toAccount=" + toAccount +
            ", value=" + value +
            ", status=" + status +
            '}';
    }
}
