package test.money.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Created by dzharikhin on 18.05.2016.
 */
public class WebUtil {

    private WebUtil() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T unwrapWithNotFoundException(Optional<T> container) {
        return container.orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T unwrapWithBadRequstException(Optional<T> container) {
        return container.orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build()));
    }
}
