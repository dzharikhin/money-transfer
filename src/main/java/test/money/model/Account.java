package test.money.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public class Account implements Comparable<Account> {
    private UUID id;
    private String label;
    private Value value;

    //jackson
    public Account() {
    }

    public Account(UUID id, String label, Value value) {
        this.id = id;
        this.label = label;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Account o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "Account{" +
            "id=" + id +
            ", label='" + label + '\'' +
            ", value=" + value +
            '}';
    }
}
