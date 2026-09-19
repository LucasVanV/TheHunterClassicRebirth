package fr.cactus.exception.launcher;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ResteasyReactiveViolationExceptionMapper
        implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {

        List<FieldErrorResponse> errors = exception
                .getConstraintViolations()
                .stream()
                .map(FieldErrorResponse::from)
                .toList();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiErrorResponse.validation(errors))
                .build();
    }
}