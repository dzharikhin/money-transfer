package test.money.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by dzharikhin on 18.05.2016.
 */
public class Value {

    private BigDecimal sum;

    //jackson
    public Value() {
    }

    public Value(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(sum, value.sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sum);
    }

    @Override
    public String toString() {
        return "Value{" +
            "sum=" + sum +
            '}';
    }
}
