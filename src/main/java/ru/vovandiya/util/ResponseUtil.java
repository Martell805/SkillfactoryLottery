package ru.vovandiya.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class ResponseUtil {
    public Response execute(Supplier<Response> action) {
        try {
            return action.get();
        } catch (WebApplicationException exception) {
            return toErrorResponse(exception);


        } catch (IllegalArgumentException exception){
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (RuntimeException exception) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Unexpected server error"))
                    .build();
        }
    }

    private Response toErrorResponse(WebApplicationException exception) {
        Response originalResponse = exception.getResponse();
        int status = originalResponse != null ? originalResponse.getStatus() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        Object entity = originalResponse != null ? originalResponse.getEntity() : null;
        String message = entity instanceof String stringEntity && !stringEntity.isBlank()
                ? stringEntity
                : exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "Request failed";
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build();
    }

}
