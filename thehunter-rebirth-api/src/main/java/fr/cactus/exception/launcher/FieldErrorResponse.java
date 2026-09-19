package fr.cactus.exception.launcher;

import jakarta.validation.ConstraintViolation;

public record FieldErrorResponse(
        String field,
        ErrorCode code
) {

    public static FieldErrorResponse from(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();

        if (field.contains(".")) {
            field = field.substring(field.lastIndexOf('.') + 1);
        }

        return new FieldErrorResponse(
                field,
                ErrorCode.valueOf(violation.getMessage())
        );
    }
}