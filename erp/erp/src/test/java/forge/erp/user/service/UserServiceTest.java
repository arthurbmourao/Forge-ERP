package forge.erp.user.service;

import forge.erp.config.ForgeSecurityProperties;
import forge.erp.permission.model.Permission;
import forge.erp.role.model.Role;
import forge.erp.role.repository.RoleRepository;
import forge.erp.user.model.User;
import forge.erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
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

    private UserService service;

    private Role viewer;
    private Role analista;
    private Role admin;

    @BeforeEach
    void setUp() {
        Permission categoryRead = permission("CATEGORY_READ");
        Permission categoryUpdate = permission("CATEGORY_UPDATE");
        Permission userRead = permission("USER_READ");

        viewer = role("VIEWER", categoryRead);
        analista = role("ANALISTA", categoryRead, categoryUpdate);
        admin = role("ADMIN", categoryRead, categoryUpdate, userRead);

        service = serviceWithBootstrap(null);
    }

    private UserService serviceWithBootstrap(String bootstrapAdmin) {
        return new UserService(userRepository, roleRepository, new ForgeSecurityProperties(bootstrapAdmin));
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

    private Jwt defaultJwt() {
        return jwt("analista", "analista@forge.local", "Analista Forge");
    }

    private User existingUser(Role role) {
        User user = new User(SUB, "analista", "analista@forge.local", "Analista Forge");
        user.setId(UUID.randomUUID());
        user.getRoles().add(role);
        return user;
    }

    // ---------- provisionamento e authorities ----------

    @Test
    @DisplayName("primeiro acesso deve criar o usuario local com o papel VIEWER e devolver so as permissoes de leitura")
    void firstAccessShouldCreateUserWithViewerRole() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.empty());
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewer));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<String> authorities = service.syncAndResolveAuthorities(defaultJwt());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_VIEWER", "CATEGORY_READ");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
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
    @DisplayName("usuario ANALISTA existente deve receber as permissoes do papel")
    void existingAnalistaShouldGetItsPermissions() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(existingUser(analista)));

        Set<String> authorities = service.syncAndResolveAuthorities(defaultJwt());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ANALISTA", "CATEGORY_READ", "CATEGORY_UPDATE");
        verify(userRepository, never()).saveAndFlush(any());
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    @DisplayName("usuario ADMIN existente deve receber tambem as permissoes de usuarios")
    void existingAdminShouldGetUserPermissions() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(existingUser(admin)));

        Set<String> authorities = service.syncAndResolveAuthorities(defaultJwt());

        assertThat(authorities).contains("ROLE_ADMIN", "USER_READ", "CATEGORY_UPDATE");
    }

    @Test
    @DisplayName("usuario inativo nao deve receber nenhuma authority")
    void inactiveUserShouldGetNoAuthorities() {
        User user = existingUser(analista);
        user.setActive(false);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        assertThat(service.syncAndResolveAuthorities(defaultJwt())).isEmpty();
    }

    @Test
    @DisplayName("deve atualizar username, e-mail e nome quando mudaram no Keycloak")
    void shouldUpdateProfileWhenChanged() {
        User user = existingUser(viewer);
        LocalDateTime before = user.getUpdatedAt();
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
        LocalDateTime before = user.getUpdatedAt();
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        service.syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getUpdatedAt()).isEqualTo(before);
    }

    // ---------- bootstrap do primeiro ADMIN (FORGE_BOOTSTRAP_ADMIN) ----------

    @Test
    @DisplayName("bootstrap: deve promover a ADMIN o usuario configurado enquanto nao existir nenhum ADMIN")
    void bootstrapShouldPromoteConfiguredUserWhenNoAdminExists() {
        User user = existingUser(viewer);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesName("ADMIN")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

        Set<String> authorities = serviceWithBootstrap("analista").syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getRoles()).containsExactly(admin);
        assertThat(authorities).contains("ROLE_ADMIN", "USER_READ");
        assertThat(authorities).doesNotContain("ROLE_VIEWER");
    }

    @Test
    @DisplayName("bootstrap: no primeiro acesso do usuario configurado ele ja nasce ADMIN")
    void bootstrapShouldPromoteOnFirstAccess() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.empty());
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewer));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsByRolesName("ADMIN")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

        Set<String> authorities = serviceWithBootstrap("analista").syncAndResolveAuthorities(defaultJwt());

        assertThat(authorities).contains("ROLE_ADMIN", "USER_READ");
    }

    @Test
    @DisplayName("bootstrap: a comparacao do username ignora maiusculas e espacos")
    void bootstrapShouldIgnoreCaseAndSpaces() {
        User user = existingUser(viewer);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesName("ADMIN")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

        serviceWithBootstrap("  ANALISTA ").syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getRoles()).containsExactly(admin);
    }

    @Test
    @DisplayName("bootstrap: nao deve promover quando ja existe um ADMIN (nao repromove quem foi rebaixado)")
    void bootstrapShouldDoNothingWhenAnAdminAlreadyExists() {
        User user = existingUser(viewer);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesName("ADMIN")).thenReturn(true);

        Set<String> authorities = serviceWithBootstrap("analista").syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getRoles()).containsExactly(viewer);
        assertThat(authorities).contains("ROLE_VIEWER").doesNotContain("ROLE_ADMIN");
        verify(roleRepository, never()).findByName("ADMIN");
    }

    @Test
    @DisplayName("bootstrap: nao deve promover um usuario diferente do configurado")
    void bootstrapShouldIgnoreOtherUsers() {
        User user = existingUser(viewer);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        serviceWithBootstrap("outra.pessoa").syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getRoles()).containsExactly(viewer);
        verify(userRepository, never()).existsByRolesName(any());
    }

    @Test
    @DisplayName("bootstrap: variavel vazia ou ausente desabilita o recurso")
    void bootstrapShouldBeDisabledWhenBlank() {
        User user = existingUser(viewer);
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(user));

        serviceWithBootstrap("").syncAndResolveAuthorities(defaultJwt());
        serviceWithBootstrap("   ").syncAndResolveAuthorities(defaultJwt());
        serviceWithBootstrap(null).syncAndResolveAuthorities(defaultJwt());

        assertThat(user.getRoles()).containsExactly(viewer);
        verify(userRepository, never()).existsByRolesName(any());
    }

    @Test
    @DisplayName("bootstrap: deve falhar com mensagem clara se o papel ADMIN nao existir")
    void bootstrapShouldFailWhenAdminRoleMissing() {
        when(userRepository.findByKeycloakId(SUB)).thenReturn(Optional.of(existingUser(viewer)));
        when(userRepository.existsByRolesName("ADMIN")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceWithBootstrap("analista").syncAndResolveAuthorities(defaultJwt()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN");
    }
}
