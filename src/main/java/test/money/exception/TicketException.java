package test.money.exception;

import test.money.model.Ticket;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public class TicketException extends WebApplicationException {

    public TicketException(Ticket ticket) {
        super(MessageFormat.format("{0} failed", ticket), Response.Status.BAD_REQUEST);
    }
}
