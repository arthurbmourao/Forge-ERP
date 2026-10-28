package forge.erp.category.controller;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(service)).build();
    }

    private CategoryResponse sampleResponse(UUID id) {
        return new CategoryResponse(id, "CAT-0149", "Fixadores", "Parafusos", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /category deve retornar 201")
    void createShouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.saveCategory(any(CategoryRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fixadores\",\"description\":\"Parafusos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CAT-0149"));
    }

    @Test
    @DisplayName("GET /category/{id} deve funcionar com o id apenas no path e retornar 200")
    void findByIdShouldUsePathVariableAndReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/category/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /category deve retornar 200 com a lista")
    void findAllShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT-0149"));
    }

    @Test
    @DisplayName("PUT /category?id= (update) deve continuar recebendo o id por query param")
    void updateShouldKeepQueryParam() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(eq(id), any(CategoryRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(put("/category").param("id", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fixadores\",\"description\":\"Parafusos\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /category/{id} deve funcionar com o id apenas no path e retornar 204")
    void deleteShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/category/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).deleteById(id);
    }

    @Test
    @DisplayName("PUT /category/{id}/activate deve funcionar com o id apenas no path")
    void activateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/category/{id}/activate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOn(id);
    }

    @Test
    @DisplayName("PUT /category/{id}/desactivate deve funcionar com o id apenas no path")
    void deactivateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/category/{id}/desactivate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOff(id);
    }
}
