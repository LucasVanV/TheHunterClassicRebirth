package fr.cactus.exception.launcher;

import jakarta.ws.rs.core.Response;

public enum ErrorCode {

    // Validation
    VALIDATION_ERROR(Response.Status.BAD_REQUEST),
    EMAIL_REQUIRED(Response.Status.BAD_REQUEST),
    EMAIL_INVALID(Response.Status.BAD_REQUEST),
    EMAIL_TOO_LONG(Response.Status.BAD_REQUEST),
    PASSWORD_REQUIRED(Response.Status.BAD_REQUEST),
    PASSWORD_INVALID(Response.Status.BAD_REQUEST),

    // Auth
    EMAIL_ALREADY_IN_USE(Response.Status.CONFLICT),
    EMAIL_NOT_FOUND(Response.Status.UNAUTHORIZED),
    INVALID_PASSWORD(Response.Status.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED(Response.Status.FORBIDDEN),
    REFRESH_TOKEN_REQUIRED(Response.Status.BAD_REQUEST),
    INVALID_REFRESH_TOKEN(Response.Status.UNAUTHORIZED),

    // Account
    EMAIL_ALREADY_VERIFIED(Response.Status.CONFLICT),
    VERIFICATION_CODE_REQUIRED(Response.Status.BAD_REQUEST),
    VERIFICATION_CODE_INVALID(Response.Status.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED(Response.Status.GONE),
    CURRENT_PASSWORD_REQUIRED(Response.Status.BAD_REQUEST),
    NEW_PASSWORD_REQUIRED(Response.Status.BAD_REQUEST),
    NEW_PASSWORD_INVALID(Response.Status.BAD_REQUEST),
    INVALID_CURRENT_PASSWORD(Response.Status.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_CURRENT(Response.Status.BAD_REQUEST),

    // Hunter
    HUNTER_NOT_FOUND(Response.Status.NOT_FOUND),
    HUNTER_ALREADY_EXISTS(Response.Status.CONFLICT),
    HUNTER_HANDLE_ALREADY_IN_USE(Response.Status.CONFLICT),

    HUNTER_HANDLE_REQUIRED(Response.Status.BAD_REQUEST),
    HUNTER_HANDLE_INVALID(Response.Status.BAD_REQUEST),
    HUNTER_GENDER_REQUIRED(Response.Status.BAD_REQUEST),
    HUNTER_FACE_REQUIRED(Response.Status.BAD_REQUEST),
    HUNTER_BANNER_REQUIRED(Response.Status.BAD_REQUEST),
    HUNTER_PROFILE_PICTURE_REQUIRED(Response.Status.BAD_REQUEST),
    HUNTER_PROFILE_PICTURE_INVALID(Response.Status.BAD_REQUEST),

    // Security
    UNAUTHORIZED(Response.Status.UNAUTHORIZED),
    FORBIDDEN(Response.Status.FORBIDDEN),

    // Internal
    INTERNAL_ERROR(Response.Status.INTERNAL_SERVER_ERROR);

    private final Response.Status status;

    ErrorCode(Response.Status status) {
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }

    public int getStatusCode() {
        return status.getStatusCode();
    }
}