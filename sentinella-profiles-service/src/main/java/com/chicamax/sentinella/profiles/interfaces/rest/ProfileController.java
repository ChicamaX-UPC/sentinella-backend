package com.chicamax.sentinella.profiles.interfaces.rest;

import com.chicamax.sentinella.profiles.domain.services.ProfileCommandService;
import com.chicamax.sentinella.profiles.interfaces.rest.resources.PatchProfileResource;
import com.chicamax.sentinella.profiles.interfaces.rest.resources.ProfileResource;
import com.chicamax.sentinella.profiles.interfaces.rest.transform.ProfileResourceAssembler;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/profiles")
public class ProfileController {

    private final ProfileCommandService profileCommandService;
    private final ProfileResourceAssembler profileResourceAssembler;

    public ProfileController(
            ProfileCommandService profileCommandService,
            ProfileResourceAssembler profileResourceAssembler
    ) {
        this.profileCommandService = profileCommandService;
        this.profileResourceAssembler = profileResourceAssembler;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResource> getMe(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var profile = profileCommandService.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        return ResponseEntity.ok(profileResourceAssembler.toResource(profile));
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResource> patchMe(Jwt jwt, @Valid @RequestBody PatchProfileResource resource) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var updated = profileCommandService.update(profileResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(profileResourceAssembler.toResource(updated));
    }
}
