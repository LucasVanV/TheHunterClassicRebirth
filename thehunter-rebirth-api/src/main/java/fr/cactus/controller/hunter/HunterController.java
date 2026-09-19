package fr.cactus.controller.hunter;

import fr.cactus.dto.hunter.CreateHunterRequest;
import fr.cactus.dto.hunter.HunterResponse;
import fr.cactus.dto.hunter.UpdateHunterProfilePictureRequest;
import fr.cactus.dto.hunter.UpdateHunterProfileRequest;
import fr.cactus.model.hunter.Hunter;
import fr.cactus.model.launcher.User;
import fr.cactus.service.hunter.HunterService;
import fr.cactus.service.launcher.AccountService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/api/hunters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("HUNTER")
public class HunterController {

    @Inject
    HunterService hunterService;

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jwt;

    @POST
    public Response create(
            @Valid CreateHunterRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        if (hunterService.findByUserId(userId).isPresent()) {
            throw new WebApplicationException(
                    "Hunter already exists",
                    Response.Status.CONFLICT
            );
        }

        if (hunterService.handleExists(request.handle())) {
            throw new WebApplicationException(
                    "Handle already in use",
                    Response.Status.CONFLICT
            );
        }

        User user = accountService.getUser(userId);

        Hunter hunter = hunterService.create(
                user,
                request.handle(),
                request.gender(),
                request.face()
        );

        return Response
                .status(Response.Status.CREATED)
                .entity(HunterResponse.from(hunter))
                .build();
    }

    @GET
    @Path("/me")
    public HunterResponse getCurrentHunter() {
        return HunterResponse.from(
                getAuthenticatedHunter()
        );
    }

    @PUT
    @Path("/me/profile")
    public HunterResponse updateProfile(
            @Valid UpdateHunterProfileRequest request
    ) {
        Hunter hunter = getAuthenticatedHunter();

        if (!hunter.handle.equals(request.handle())
                && hunterService.handleExists(request.handle())) {

            throw new WebApplicationException(
                    "Handle already in use",
                    Response.Status.CONFLICT
            );
        }

        Hunter updatedHunter = hunterService.updateProfile(
                hunter,
                request.handle(),
                request.gender(),
                request.face(),
                request.bannerId()
        );

        return HunterResponse.from(updatedHunter);
    }

    @PUT
    @Path("/me/profile-picture")
    public HunterResponse updateProfilePicture(
            @Valid UpdateHunterProfilePictureRequest request
    ) {
        Hunter hunter = getAuthenticatedHunter();

        Hunter updatedHunter =
                hunterService.updateProfilePicture(
                        hunter,
                        request.profilePicture()
                );

        return HunterResponse.from(updatedHunter);
    }

    private UUID getAuthenticatedUserId() {
        return UUID.fromString(jwt.getSubject());
    }

    private Hunter getAuthenticatedHunter() {
        UUID userId = getAuthenticatedUserId();

        return hunterService
                .findByUserId(userId)
                .orElseThrow(NotFoundException::new);
    }
}