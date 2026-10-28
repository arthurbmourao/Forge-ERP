package forge.erp.unitconversion.controller;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ConflictException;
import forge.erp.common.exception.GlobalExceptionHandler;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.service.UnitConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UnitConversionControllerTest {

    @Mock
    private UnitConversionService service;

    private MockMvc mockMvc;

    private final UUID fromId = UUID.randomUUID();
    private final UUID toId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UnitConversionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String body() {
        return """
                {
                  "fromUnitId": "%s",
                  "toUnitId": "%s",
                  "factor": 1000,
                  "active": true
                }
                """.formatted(fromId, toId);
    }

    private UnitConversionResponse sampleResponse(UUID id) {
        return new UnitConversionResponse(id, fromId, "UNI-0001", "kg", toId, "UNI-0002", "g",
                new BigDecimal("1000"), true, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /unit-conversion deve devolver a resposta plana, sem a entidade Unit inteira")
    void createShouldReturnFlatResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.saveUnitConversion(any(UnitConversionRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromUnitId").value(fromId.toString()))
                .andExpect(jsonPath("$.fromUnitSymbol").value("kg"))
                .andExpect(jsonPath("$.toUnitCode").value("UNI-0002"))
                .andExpect(jsonPath("$.fromUnit").doesNotExist())
                .andExpect(jsonPath("$.toUnit").doesNotExist());
    }

    @Test
    @DisplayName("POST /unit-conversion com dados invalidos deve retornar 400 sem chamar o service")
    void createWithInvalidBodyShouldReturn400() throws Exception {
        String invalid = """
                { "fromUnitId": null, "toUnitId": "%s", "factor": -5, "active": true }
                """.formatted(toId);

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fromUnitId").exists())
                .andExpect(jsonPath("$.errors.factor").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("PUT /unit-conversion/{id} com fator zero deve retornar 400 sem chamar o service")
    void updateWithInvalidBodyShouldReturn400() throws Exception {
        String invalid = """
                { "fromUnitId": "%s", "toUnitId": "%s", "factor": 0, "active": true }
                """.formatted(fromId, toId);

        mockMvc.perform(put("/unit-conversion/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /unit-conversion deve retornar 422 quando as unidades violam uma regra (KG -> M)")
    void createShouldReturn422OnBusinessRule() throws Exception {
        when(service.saveUnitConversion(any(UnitConversionRequest.class)))
                .thenThrow(new BusinessRuleException("As unidades devem pertencer ao mesmo tipo."));

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /unit-conversion deve retornar 409 quando o par ja existe")
    void createShouldReturn409OnDuplicate() throws Exception {
        when(service.saveUnitConversion(any(UnitConversionRequest.class)))
                .thenThrow(new ConflictException("Já existe uma conversão entre essas unidades."));

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /unit-conversion deve receber os ids das unidades e retornar 201")
    void createShouldAcceptUnitIds() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.saveUnitConversion(any(UnitConversionRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));

        ArgumentCaptor<UnitConversionRequest> captor = ArgumentCaptor.forClass(UnitConversionRequest.class);
        verify(service).saveUnitConversion(captor.capture());
        assertThat(captor.getValue().fromUnitId()).isEqualTo(fromId);
        assertThat(captor.getValue().toUnitId()).isEqualTo(toId);
        assertThat(captor.getValue().factor()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("POST /unit-conversion deve retornar 404 quando uma unidade nao existe")
    void createShouldReturn404WhenUnitMissing() throws Exception {
        when(service.saveUnitConversion(any(UnitConversionRequest.class)))
                .thenThrow(ResourceNotFoundException.of("Unidade", fromId));

        mockMvc.perform(post("/unit-conversion").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /unit-conversion deve retornar 200 com a lista")
    void getAllShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/unit-conversion"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /unit-conversion/{id} deve funcionar com o id apenas no path")
    void findByIdShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/unit-conversion/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("PUT /unit-conversion/{id} deve funcionar com o id apenas no path")
    void updateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(any(UnitConversionRequest.class), eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(put("/unit-conversion/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("PUT /unit-conversion/{id}/activate deve funcionar com o id apenas no path")
    void activateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/unit-conversion/{id}/activate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOn(id);
    }

    @Test
    @DisplayName("PUT /unit-conversion/{id}/desactivate deve funcionar com o id apenas no path")
    void deactivateShouldUsePathVariable() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/unit-conversion/{id}/desactivate", id))
                .andExpect(status().isNoContent());

        verify(service).turnOff(id);
    }
}
