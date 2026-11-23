package forge.erp.user.controller;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.GlobalExceptionHandler;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.user.dto.UserResponse;
import forge.erp.user.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Principal ADMIN = () -> "admin-sub";

    @Mock
    private UserManagementService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponse sampleResponse(UUID id, String role) {
        return new UserResponse(id, "sub-1", "ana", "ana@forge.local", "Ana", role, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /users deve retornar 200 com a lista")
    void findAllShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse(UUID.randomUUID(), "VIEWER")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana"))
                .andExpect(jsonPath("$[0].role").value("VIEWER"));
    }

    @Test
    @DisplayName("GET /users/{id} deve retornar 200 e 404")
    void findByIdShouldReturn200And404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        when(service.findById(id)).thenReturn(sampleResponse(id, "ANALISTA"));
        when(service.findById(missing)).thenThrow(ResourceNotFoundException.of("Usuário", missing));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.role").value("ANALISTA"));
        mockMvc.perform(get("/users/{id}", missing))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /users/{id}/role deve repassar o sub de quem executa e retornar 200")
    void changeRoleShouldPassTheActingUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.changeRole(id, "ANALISTA", "admin-sub")).thenReturn(sampleResponse(id, "ANALISTA"));

        mockMvc.perform(put("/users/{id}/role", id)
                        .principal(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ANALISTA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ANALISTA"));

        verify(service).changeRole(id, "ANALISTA", "admin-sub");
    }

    @Test
    @DisplayName("PUT /users/{id}/role sem o papel deve retornar 400 sem chamar o service")
    void changeRoleWithoutRoleShouldReturn400() throws Exception {
        mockMvc.perform(put("/users/{id}/role", UUID.randomUUID())
                        .principal(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("PUT /users/{id}/role deve retornar 422 quando a regra de negocio e violada")
    void changeRoleShouldReturn422OnBusinessRule() throws Exception {
        when(service.changeRole(any(UUID.class), eq("VIEWER"), eq("admin-sub")))
                .thenThrow(new BusinessRuleException("Você não pode alterar o seu próprio papel."));

        mockMvc.perform(put("/users/{id}/role", UUID.randomUUID())
                        .principal(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"VIEWER\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PATCH /users/{id}/activate deve retornar 204")
    void activateShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/users/{id}/activate", id).principal(ADMIN))
                .andExpect(status().isNoContent());

        verify(service).activate(id);
    }

    @Test
    @DisplayName("PATCH /users/{id}/deactivate deve repassar o sub de quem executa e retornar 204")
    void deactivateShouldPassTheActingUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/users/{id}/deactivate", id).principal(ADMIN))
                .andExpect(status().isNoContent());

        verify(service).deactivate(id, "admin-sub");
    }

    @Test
    @DisplayName("PATCH /users/{id}/deactivate deve retornar 422 ao tentar desativar a si mesmo")
    void deactivateSelfShouldReturn422() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new BusinessRuleException("Você não pode desativar o seu próprio usuário."))
                .when(service).deactivate(id, "admin-sub");

        mockMvc.perform(patch("/users/{id}/deactivate", id).principal(ADMIN))
                .andExpect(status().isUnprocessableEntity());
    }
}
