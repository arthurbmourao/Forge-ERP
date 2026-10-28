package forge.erp.unit.controller;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.service.UnitService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UnitControllerTest {

    @Mock
    private UnitService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UnitController(service)).build();
    }

    private UnitDtoResponse sampleResponse(UUID id) {
        return new UnitDtoResponse(id, "Quilograma", "UNI-0125", "kg", UnitType.MASS, 3, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /unit deve retornar 201")
    void createShouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.saveUnit(any(UnitDtoRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/unit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Quilograma\",\"symbol\":\"kg\",\"type\":\"MASS\","
                                + "\"decimalPlaces\":3,\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("UNI-0125"));
    }

    @Test
    @DisplayName("GET /unit deve retornar 200 com a lista")
    void getAllShouldReturn200() throws Exception {
        when(service.getAll()).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/unit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("kg"));
    }

    @Test
    @DisplayName("GET /unit/{id} deve funcionar com o id apenas no path")
    void findByIdShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/unit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("DELETE /unit/{id} deve funcionar com o id apenas no path e retornar 204")
    void deleteShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/unit/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).deleteById(id);
    }

    @Test
    @DisplayName("PUT /unit/{id}/activate deve funcionar com o id apenas no path")
    void activateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/unit/{id}/activate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOn(id);
    }

    @Test
    @DisplayName("PUT /unit/{id}/desactivate deve funcionar com o id apenas no path")
    void deactivateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/unit/{id}/desactivate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOff(id);
    }
}
