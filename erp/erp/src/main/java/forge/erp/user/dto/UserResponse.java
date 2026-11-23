package forge.erp.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,

        // identificador do usuario no Keycloak (claim "sub")
        String keycloakId,

        String username,

        String email,

        String name,

        // cada usuario tem um unico papel (ADMIN, ANALISTA ou VIEWER)
        String role,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
