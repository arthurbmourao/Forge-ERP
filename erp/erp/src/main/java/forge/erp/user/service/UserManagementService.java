package forge.erp.user.service;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.role.model.Role;
import forge.erp.role.repository.RoleRepository;
import forge.erp.user.dto.UserResponse;
import forge.erp.user.mapper.UserMapper;
import forge.erp.user.model.User;
import forge.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestao de usuarios por um administrador: consultar, trocar o papel e ativar/desativar.
 * Usuarios nao sao criados aqui: nascem no Keycloak e entram no ERP no primeiro acesso.
 * Papeis e permissoes sao fixos (definidos por migration).
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByUsernameAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    /**
     * Define o unico papel do usuario.
     *
     * @param actingKeycloakId sub de quem faz a alteracao; ninguem altera o proprio papel, o que
     *                         impede o sistema de ficar sem administrador
     */
    @Transactional
    public UserResponse changeRole(UUID id, String roleName, String actingKeycloakId) {
        User user = getOrThrow(id);

        if (user.getKeycloakId().equals(actingKeycloakId)) {
            throw new BusinessRuleException("Você não pode alterar o seu próprio papel.");
        }

        Role role = findRole(roleName);

        user.getRoles().clear();
        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void activate(UUID id) {
        User user = getOrThrow(id);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * @param actingKeycloakId sub de quem faz a alteracao; ninguem desativa a si mesmo
     */
    @Transactional
    public void deactivate(UUID id, String actingKeycloakId) {
        User user = getOrThrow(id);

        if (user.getKeycloakId().equals(actingKeycloakId)) {
            throw new BusinessRuleException("Você não pode desativar o seu próprio usuário.");
        }

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private User getOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuário", id));
    }

    private Role findRole(String roleName) {
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        return roleRepository.findByName(normalized)
                .orElseThrow(() -> new BusinessRuleException(
                        "Papel inexistente: '%s'. Papéis válidos: %s.".formatted(roleName, validRoles())));
    }

    private String validRoles() {
        return roleRepository.findAll().stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
