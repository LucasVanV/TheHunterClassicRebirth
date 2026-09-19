package fr.cactus.exception.launcher;

import java.util.List;

public record ApiErrorResponse(
        int status,
        ErrorCode code,
        List<FieldErrorResponse> errors
) {

    public static ApiErrorResponse from(ApiException exception) {
        return new ApiErrorResponse(
                exception.getStatus(),
                exception.getCode(),
                null
        );
    }

    public static ApiErrorResponse validation(
            List<FieldErrorResponse> errors
    ) {
        return new ApiErrorResponse(
                ErrorCode.VALIDATION_ERROR.getStatusCode(),
                ErrorCode.VALIDATION_ERROR,
                errors
        );
    }
}