package fr.cactus.controller.launcher;

import fr.cactus.dto.launcher.account.ChangeEmailRequest;
import fr.cactus.dto.launcher.account.ChangePasswordRequest;
import fr.cactus.dto.launcher.account.DeleteAccountRequest;
import fr.cactus.dto.launcher.account.VerifyEmailRequest;
import fr.cactus.service.global.CurrentUserService;
import fr.cactus.service.launcher.AccountService;
import fr.cactus.service.launcher.EmailVerificationService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/account")
@RolesAllowed("HUNTER")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {

    @Inject
    CurrentUserService currentUserService;

    @Inject
    AccountService accountService;

    @Inject
    EmailVerificationService emailVerificationService;

    @POST
    @Path("/email-verification/send")
    public Response sendEmailVerificationCode() {
        emailVerificationService.sendCode(
                currentUserService.getUserId()
        );

        return Response
                .noContent()
                .build();
    }

    @POST
    @Path("/email-verification/verify")
    public Response verifyEmail(
            @Valid VerifyEmailRequest request
    ) {
        emailVerificationService.verify(
                currentUserService.getUserId(),
                request.code()
        );

        return Response
                .noContent()
                .build();
    }

    @PATCH
    @Path("/email")
    public Response changeEmail(
            @Valid ChangeEmailRequest request
    ) {
        accountService.changeEmail(
                currentUserService.getUserId(),
                request
        );

        return Response
                .noContent()
                .build();
    }

    @PATCH
    @Path("/password")
    public Response changePassword(
            @Valid ChangePasswordRequest request
    ) {
        accountService.changePassword(
                currentUserService.getUserId(),
                request
        );

        return Response
                .noContent()
                .build();
    }

    @DELETE
    public Response deleteAccount(
            @Valid DeleteAccountRequest request
    ) {
        accountService.deleteAccount(
                currentUserService.getUserId(),
                request
        );

        return Response
                .noContent()
                .build();
    }
}