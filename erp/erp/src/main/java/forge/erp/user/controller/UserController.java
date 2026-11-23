package forge.erp.user.controller;

import forge.erp.user.dto.UserResponse;
import forge.erp.user.dto.UserRoleRequest;
import forge.erp.user.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Gestao de usuarios (perfil local). O nome do Principal e o sub do token no Keycloak.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserManagementService service;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserResponse> changeRole(@PathVariable UUID id,
                                                   @Valid @RequestBody UserRoleRequest request,
                                                   Principal principal) {
        return ResponseEntity.ok(service.changeRole(id, request.role(), principal.getName()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id, Principal principal) {
        service.deactivate(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
