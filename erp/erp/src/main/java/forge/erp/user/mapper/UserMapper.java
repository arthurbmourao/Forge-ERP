package forge.erp.user.mapper;

import forge.erp.role.model.Role;
import forge.erp.user.dto.UserResponse;
import forge.erp.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        // o banco garante um unico papel por usuario (indice unico em user_roles.user_id)
        String role = user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .findFirst()
                .orElse(null);

        return new UserResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                role,
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
