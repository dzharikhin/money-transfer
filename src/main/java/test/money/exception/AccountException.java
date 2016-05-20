package test.money.exception;

import test.money.model.Account;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

/**
 * Created by dzharikhin on 18.05.2016.
 */
public class AccountException extends WebApplicationException {

    public AccountException(Account account) {
        super(MessageFormat.format("{0}", account), Response.Status.BAD_REQUEST);
    }
}
