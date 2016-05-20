package test.money.resource;

import org.junit.Test;
import test.money.model.Account;
import test.money.model.Value;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;


/**
 * Created by dzharikhin on 19.05.2016.
 */
public class AccountResourceTest extends AbstractMoneyRestTest {

    @Test
    public void successToListAccountWithFilter() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        Account account2 = new Account(UUID.randomUUID(), "test2", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        accountStorage.put(account2.getId(), account2);
        List<Account> actual = target()
            .path("accounts")
            .queryParam("labelFilter", new Object[]{"test"})
            .request().get()
            .readEntity(ACCOUNT_LIST_TYPE);
        assertThat(actual, containsInAnyOrder(account));
    }

    @Test
    public void successToCreateNewAccount() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        Account result = target()
            .path("accounts/" + account.getId())
            .request()
            .post(null)
            .readEntity(ACCOUNT_TYPE);
        assertThat(result, is(account));
    }

    @Test
    public void failsToCreateExistingAccount() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        Response result = target()
            .path("accounts/" + account.getId())
            .request()
            .post(null);
        assertThat(result.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void failsCreationIfAccountIdIsNotUUID() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        Account account2 = new Account(UUID.randomUUID(), "test2", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        accountStorage.put(account2.getId(), account2);
        Response result = target().path("accounts/abcd")
            .queryParam("result", new Object[] {"test1", "test2"})
            .request()
            .get();
        assertThat(result.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void successToGetExistingAccount() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        Account result = target().path("accounts/" + account.getId())
            .request()
            .get()
            .readEntity(ACCOUNT_TYPE);
        assertThat(result, is(account));
    }

    @Test
    public void successToUpdateExistingAccount() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        Account result = target().path("accounts/" + account.getId())
            .queryParam("label", "newTest")
            .request()
            .put(null)
            .readEntity(ACCOUNT_TYPE);
        assertThat(result.getLabel(), is("newTest"));
    }

    @Test
    public void successToDeleteExistingAccount() {
        Account account = new Account(UUID.randomUUID(), "test", new Value(BigDecimal.ZERO));
        accountStorage.put(account.getId(), account);
        Response result = target().path("accounts/" + account.getId())
            .queryParam("label", "newTest")
            .request()
            .delete();
        assertThat(result.getStatus(), is(Response.Status.OK.getStatusCode()));
    }
}
