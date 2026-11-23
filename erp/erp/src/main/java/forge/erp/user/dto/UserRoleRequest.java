package forge.erp.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRoleRequest(
        // nome do papel: ADMIN, ANALISTA ou VIEWER
        @NotBlank
        String role
) {
}
