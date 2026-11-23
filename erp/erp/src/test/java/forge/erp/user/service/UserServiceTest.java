package forge.erp.user.service;

import forge.erp.permission.model.Permission;
import forge.erp.role.model.Role;
import forge.erp.role.repository.RoleRepository;
import forge.erp.user.model.User;
import forge.erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String SUB = "3f2b1c8e-5a47-4d6e-9b1a-0c7d2e4f6a11";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService service;

    private Role viewer;
    private Role analista;

    @BeforeEach
    void setUp() {
        Permission categoryRead = permission("CATEGORY_READ");
        Permission categoryWrite = permission("CATEGORY_WRITE");

        viewer = role("VIEWER", categoryRead);
        analista = role("ANALISTA", categoryRead, categoryWrite);
    }

    private Permission permission(String name) {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setName(name);
        return permission;
    }

    private Role role(String name, Permission... permissions) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        role.getPermissions().addAll(Set.of(permissions));
        return role;
    }

    private Jwt jwt(String username, String email, String name) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(SUB)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        if (username != null) builder.claim("preferred_username", username);
        if (email != null) builder.claim("email", email);
        if (name != null) builder.claim("name", name);
        return builder.build();
    }

    private User existingUser(Role role) {
        User user = new User(SUB, "analista", "analista@forge.local", "Analista Forge");
        user.setId(UUID.randomUUID());
        user.getRoles().add(role);
        return user;
    }

    @Test
    @DisplayName("primeiro acesso deve criar o usuario local com o papel VIEWER e devolver so as permissoes de leitura")
    void firstAccessShouldCreateUserWithViewerRole() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.empty());
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewer));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<String> authorities = service.syncAndResolveAuthorities(
                jwt("analista", "analista@forge.local", "Analista Forge"));

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_VIEWER", "CATEGORY_READ");

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User created = captor.getValue();
        assertThat(created.getKeycloakId()).isEqualTo(SUB);
        assertThat(created.getUsername()).isEqualTo("analista");
        assertThat(created.getEmail()).isEqualTo("analista@forge.local");
        assertThat(created.getName()).isEqualTo("Analista Forge");
        assertThat(created.isActive()).isTrue();
        assertThat(created.getRoles()).containsExactly(viewer);
    }

    @Test
    @DisplayName("primeiro acesso deve falhar com mensagem clara se o papel padrao nao existir")
    void firstAccessShouldFailWhenDefaultRoleMissing() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.empty());
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.syncAndResolveAuthorities(jwt("a", "a@a", "A")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VIEWER");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("usuario ANALISTA existente deve receber as permissoes de leitura e escrita")
    void existingAnalistaShouldGetWritePermissions() {
        User user = existingUser(analista);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        Set<String> authorities = service.syncAndResolveAuthorities(
                jwt("analista", "analista@forge.local", "Analista Forge"));

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ANALISTA", "CATEGORY_READ", "CATEGORY_WRITE");
        verify(userRepository, never()).saveAndFlush(any());
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    @DisplayName("usuario inativo nao deve receber nenhuma authority")
    void inactiveUserShouldGetNoAuthorities() {
        User user = existingUser(analista);
        user.setActive(false);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        Set<String> authorities = service.syncAndResolveAuthorities(
                jwt("analista", "analista@forge.local", "Analista Forge"));

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("deve atualizar username, e-mail e nome quando mudaram no Keycloak")
    void shouldUpdateProfileWhenChanged() {
        User user = existingUser(viewer);
        java.time.LocalDateTime before = user.getUpdatedAt();
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        service.syncAndResolveAuthorities(jwt("novo.user", "novo@forge.local", "Novo Nome"));

        assertThat(user.getUsername()).isEqualTo("novo.user");
        assertThat(user.getEmail()).isEqualTo("novo@forge.local");
        assertThat(user.getName()).isEqualTo("Novo Nome");
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("nao deve alterar o usuario quando o perfil nao mudou")
    void shouldNotTouchUserWhenProfileUnchanged() {
        User user = existingUser(viewer);
        java.time.LocalDateTime before = user.getUpdatedAt();
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        service.syncAndResolveAuthorities(jwt("analista", "analista@forge.local", "Analista Forge"));

        assertThat(user.getUpdatedAt()).isEqualTo(before);
    }
}
