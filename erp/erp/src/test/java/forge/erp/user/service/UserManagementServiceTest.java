package forge.erp.user.service;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.role.model.Role;
import forge.erp.role.repository.RoleRepository;
import forge.erp.user.dto.UserResponse;
import forge.erp.user.mapper.UserMapper;
import forge.erp.user.model.User;
import forge.erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    private static final String ADMIN_SUB = "admin-sub";
    private static final String OTHER_SUB = "other-sub";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserManagementService service;

    private Role viewer;
    private Role analista;
    private Role admin;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userRepository, roleRepository, new UserMapper());
        viewer = role("VIEWER");
        analista = role("ANALISTA");
        admin = role("ADMIN");
    }

    private Role role(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    private User user(String sub, String username, Role role) {
        User user = new User(sub, username, username + "@forge.local", username);
        user.setId(UUID.randomUUID());
        user.getRoles().add(role);
        return user;
    }

    // ---------- consulta ----------

    @Test
    @DisplayName("findAll deve devolver os usuarios com o papel de cada um")
    void findAllShouldMapUsersWithRole() {
        User a = user(OTHER_SUB, "ana", viewer);
        User b = user(ADMIN_SUB, "bia", admin);
        when(userRepository.findAllByOrderByUsernameAsc()).thenReturn(List.of(a, b));

        List<UserResponse> result = service.findAll();

        assertThat(result).extracting(UserResponse::username).containsExactly("ana", "bia");
        assertThat(result).extracting(UserResponse::role).containsExactly("VIEWER", "ADMIN");
        assertThat(result.get(0).keycloakId()).isEqualTo(OTHER_SUB);
        assertThat(result.get(0).active()).isTrue();
    }

    @Test
    @DisplayName("findById deve devolver o usuario")
    void findByIdShouldReturnUser() {
        User user = user(OTHER_SUB, "ana", analista);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = service.findById(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.role()).isEqualTo("ANALISTA");
    }

    @Test
    @DisplayName("findById deve lancar 404 quando o usuario nao existe")
    void findByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- troca de papel ----------

    @Test
    @DisplayName("changeRole deve substituir o papel unico do usuario e persistir")
    void changeRoleShouldReplaceTheSingleRole() {
        User target = user(OTHER_SUB, "ana", viewer);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(roleRepository.findByName("ANALISTA")).thenReturn(Optional.of(analista));
        when(userRepository.save(target)).thenReturn(target);

        UserResponse response = service.changeRole(target.getId(), "ANALISTA", ADMIN_SUB);

        assertThat(target.getRoles()).containsExactly(analista);
        assertThat(response.role()).isEqualTo("ANALISTA");
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("changeRole deve normalizar o nome do papel (espacos e maiusculas)")
    void changeRoleShouldNormalizeRoleName() {
        User target = user(OTHER_SUB, "ana", viewer);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));
        when(userRepository.save(target)).thenReturn(target);

        UserResponse response = service.changeRole(target.getId(), "  admin ", ADMIN_SUB);

        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("changeRole nao deve permitir que o usuario altere o proprio papel")
    void changeRoleShouldRejectSelfChange() {
        User self = user(ADMIN_SUB, "bia", admin);
        when(userRepository.findById(self.getId())).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.changeRole(self.getId(), "VIEWER", ADMIN_SUB))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("próprio papel");

        assertThat(self.getRoles()).containsExactly(admin);
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeRole deve rejeitar papel inexistente listando os validos")
    void changeRoleShouldRejectUnknownRole() {
        User target = user(OTHER_SUB, "ana", viewer);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(roleRepository.findByName("GERENTE")).thenReturn(Optional.empty());
        when(roleRepository.findAll()).thenReturn(List.of(viewer, analista, admin));

        assertThatThrownBy(() -> service.changeRole(target.getId(), "gerente", ADMIN_SUB))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("gerente")
                .hasMessageContaining("ADMIN, ANALISTA, VIEWER");

        assertThat(target.getRoles()).containsExactly(viewer);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeRole deve lancar 404 quando o usuario nao existe")
    void changeRoleShouldThrowWhenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeRole(id, "VIEWER", ADMIN_SUB))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    // ---------- ativar e desativar ----------

    @Test
    @DisplayName("deactivate deve desativar outro usuario e persistir")
    void deactivateShouldDeactivateAnotherUser() {
        User target = user(OTHER_SUB, "ana", viewer);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        service.deactivate(target.getId(), ADMIN_SUB);

        assertThat(target.isActive()).isFalse();
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("deactivate nao deve permitir que o usuario desative a si mesmo")
    void deactivateShouldRejectSelf() {
        User self = user(ADMIN_SUB, "bia", admin);
        when(userRepository.findById(self.getId())).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.deactivate(self.getId(), ADMIN_SUB))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("desativar");

        assertThat(self.isActive()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivate deve lancar 404 quando o usuario nao existe")
    void deactivateShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(id, ADMIN_SUB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("activate deve reativar o usuario e persistir")
    void activateShouldReactivateUser() {
        User target = user(OTHER_SUB, "ana", viewer);
        target.setActive(false);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        service.activate(target.getId());

        assertThat(target.isActive()).isTrue();
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("activate deve lancar 404 quando o usuario nao existe")
    void activateShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
