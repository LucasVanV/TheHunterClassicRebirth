package fr.cactus.controller.launcher;

import fr.cactus.dto.launcher.auth.AuthResponse;
import fr.cactus.dto.launcher.auth.LoginRequest;
import fr.cactus.dto.launcher.auth.RefreshTokenRequest;
import fr.cactus.dto.launcher.auth.RegisterRequest;
import fr.cactus.service.launcher.AuthService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);

        return Response
                .status(Response.Status.CREATED)
                .entity(authResponse)
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        return Response
                .ok(authResponse)
                .build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refresh(
                request.refreshToken()
        );

        return Response
                .ok(authResponse)
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid RefreshTokenRequest request) {
        authService.logout(request.refreshToken());

        return Response
                .noContent()
                .build();
    }
}