package forge.erp.productunit.controller;

import forge.erp.common.exception.ConflictException;
import forge.erp.common.exception.GlobalExceptionHandler;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.productunit.dto.ProductUnitRequest;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.service.ProductUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductUnitControllerTest {

    @Mock
    private ProductUnitService service;

    private MockMvc mockMvc;

    private final UUID productId = UUID.randomUUID();
    private final UUID unitId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductUnitController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String validBody() {
        return """
                {
                  "unitId": "%s",
                  "conversionFactor": 100,
                  "purchaseUnit": true,
                  "issueUnit": false,
                  "active": true
                }
                """.formatted(unitId);
    }

    private ProductUnitResponse sampleResponse(UUID id) {
        return new ProductUnitResponse(id, productId, unitId, "UNI-0002", "CX",
                new BigDecimal("100"), true, false, true);
    }

    @Test
    @DisplayName("POST /products/{productId}/units deve retornar 201 com Location aninhado")
    void createShouldReturn201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.save(any(UUID.class), any(ProductUnitRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/products/{productId}/units", productId)
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/products/" + productId + "/units/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.unitSymbol").value("CX"));
    }

    @Test
    @DisplayName("POST com fator de conversao nao positivo deve retornar 400 sem chamar o service")
    void createWithNonPositiveFactorShouldReturn400() throws Exception {
        String invalid = """
                {
                  "unitId": "%s",
                  "conversionFactor": 0,
                  "purchaseUnit": true,
                  "issueUnit": true,
                  "active": true
                }
                """.formatted(unitId);

        mockMvc.perform(post("/products/{productId}/units", productId)
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST deve retornar 409 quando a unidade ja esta vinculada ao produto")
    void createShouldReturn409OnDuplicate() throws Exception {
        when(service.save(any(UUID.class), any(ProductUnitRequest.class)))
                .thenThrow(new ConflictException("Esta unidade já está vinculada ao produto."));

        mockMvc.perform(post("/products/{productId}/units", productId)
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /products/{productId}/units deve retornar 200 com a lista")
    void findAllShouldReturn200() throws Exception {
        when(service.findAll(productId)).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/products/{productId}/units", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitCode").value("UNI-0002"));
    }

    @Test
    @DisplayName("GET /products/{productId}/units/{id} deve retornar 404 quando nao existe")
    void findByIdShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(productId, id)).thenThrow(ResourceNotFoundException.of("Unidade do produto", id));

        mockMvc.perform(get("/products/{productId}/units/{id}", productId, id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /products/{productId}/units/{id} deve retornar 200")
    void updateShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(any(UUID.class), any(UUID.class), any(ProductUnitRequest.class)))
                .thenReturn(sampleResponse(id));

        mockMvc.perform(put("/products/{productId}/units/{id}", productId, id)
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("DELETE /products/{productId}/units/{id} deve retornar 204")
    void deleteShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/products/{productId}/units/{id}", productId, id))
                .andExpect(status().isNoContent());

        verify(service).deleteById(productId, id);
    }

    @Test
    @DisplayName("PATCH activate e deactivate devem retornar 204")
    void activateAndDeactivateShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/products/{productId}/units/{id}/activate", productId, id))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/products/{productId}/units/{id}/deactivate", productId, id))
                .andExpect(status().isNoContent());

        verify(service).turnOn(productId, id);
        verify(service).turnOff(productId, id);
    }
}
