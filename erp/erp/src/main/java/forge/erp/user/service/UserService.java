package forge.erp.user.service;

import forge.erp.config.ForgeSecurityProperties;
import forge.erp.permission.model.Permission;
import forge.erp.role.model.Role;
import forge.erp.role.repository.RoleRepository;
import forge.erp.user.model.User;
import forge.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Liga o usuario autenticado no Keycloak (claim "sub") ao perfil local, que define as permissoes.
 * Nao ha controller: e usado apenas pela autenticacao.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    public static final String DEFAULT_ROLE = "VIEWER";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ForgeSecurityProperties securityProperties;

    /**
     * Localiza o usuario local pelo sub do token (criando-o com o papel padrao no primeiro acesso),
     * atualiza nome/e-mail se mudaram no Keycloak e devolve suas authorities: os papeis
     * (prefixo ROLE_) e as permissoes. Usuario inativo nao recebe nenhuma authority.
     */
    @Transactional
    public Set<String> syncAndResolveAuthorities(Jwt jwt) {
        User user = userRepository.findByKeycloakId(jwt.getSubject())
                .orElseGet(() -> createUser(jwt));

        promoteBootstrapAdminIfNeeded(user, jwt);
        updateProfile(user, jwt);

        Set<String> authorities = new HashSet<>();
        if (!user.isActive()) {
            return authorities;
        }

        for (Role role : user.getRoles()) {
            authorities.add(ROLE_PREFIX + role.getName());
            for (Permission permission : role.getPermissions()) {
                authorities.add(permission.getName());
            }
        }
        return authorities;
    }

    private User createUser(Jwt jwt) {
        Role defaultRole = findRole(DEFAULT_ROLE);

        User user = new User(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name")
        );
        user.getRoles().add(defaultRole);

        // flush imediato: se outra requisicao criou o mesmo sub ao mesmo tempo, a violacao da
        // unicidade de keycloak_id aparece aqui e o chamador tenta de novo
        return userRepository.saveAndFlush(user);
    }

    /**
     * Torna ADMIN o usuario cujo username e o configurado em FORGE_BOOTSTRAP_ADMIN, mas somente
     * enquanto nao existir nenhum ADMIN. Assim o recurso so serve para criar o primeiro
     * administrador e nao repromove alguem que foi rebaixado depois.
     */
    private void promoteBootstrapAdminIfNeeded(User user, Jwt jwt) {
        String configured = securityProperties == null ? null : securityProperties.bootstrapAdmin();
        String username = jwt.getClaimAsString("preferred_username");

        if (configured == null || configured.isBlank() || username == null
                || !configured.trim().equalsIgnoreCase(username.trim())) {
            return;
        }
        if (userRepository.existsByRolesName(ADMIN_ROLE)) {
            return;
        }

        Role admin = findRole(ADMIN_ROLE);
        user.getRoles().clear();
        user.getRoles().add(admin);
        user.setUpdatedAt(LocalDateTime.now());
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Papel '%s' não encontrado. As migrations foram aplicadas?".formatted(name)));
    }

    private void updateProfile(User user, Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        boolean changed = !Objects.equals(user.getUsername(), username)
                || !Objects.equals(user.getEmail(), email)
                || !Objects.equals(user.getName(), name);

        if (changed) {
            user.setUsername(username);
            user.setEmail(email);
            user.setName(name);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }
}
