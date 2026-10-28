package forge.erp.product.controller;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.GlobalExceptionHandler;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.product.dto.ProductRequest;
import forge.erp.product.dto.ProductResponse;
import forge.erp.product.service.ProductService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
class ProductControllerTest {

    @Mock
    private ProductService service;

    private MockMvc mockMvc;

    private final UUID categoryId = UUID.randomUUID();
    private final UUID unitId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String validBody() {
        return """
                {
                  "name": "Parafuso Sextavado M8",
                  "description": "Parafuso de aço",
                  "categoryId": "%s",
                  "stockUnitId": "%s",
                  "weight": 0.015,
                  "minimumStock": 100,
                  "maximumStock": 1000,
                  "active": true
                }
                """.formatted(categoryId, unitId);
    }

    private ProductResponse sampleResponse(UUID id) {
        return new ProductResponse(id, "PRD-0149", "Parafuso Sextavado M8", "Parafuso de aço",
                categoryId, "Fixadores", unitId, "UN",
                new BigDecimal("0.015"), new BigDecimal("100"), new BigDecimal("1000"), true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /products deve retornar 201 com Location e o corpo criado")
    void createShouldReturn201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.saveProduct(any(ProductRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/products/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("PRD-0149"));
    }

    @Test
    @DisplayName("POST /products com dados invalidos deve retornar 400 sem chamar o service")
    void createWithInvalidBodyShouldReturn400() throws Exception {
        String invalid = """
                { "name": "", "active": true }
                """;

        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /products deve retornar 422 quando uma regra de negocio e violada")
    void createShouldReturn422OnBusinessRule() throws Exception {
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BusinessRuleException("A categoria informada está inativa."));

        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /products deve retornar 200 com a lista")
    void findAllShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PRD-0149"));
    }

    @Test
    @DisplayName("GET /products/{id} deve retornar 200 quando existe")
    void findByIdShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /products/{id} deve retornar 404 quando nao existe")
    void findByIdShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(ResourceNotFoundException.of("Produto", id));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /products/{id} deve retornar 200 com o produto atualizado")
    void updateShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(any(UUID.class), any(ProductRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(put("/products/{id}", id).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("DELETE /products/{id} deve retornar 204")
    void deleteShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).deleteById(id);
    }

    @Test
    @DisplayName("DELETE /products/{id} deve retornar 404 quando nao existe")
    void deleteShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(ResourceNotFoundException.of("Produto", id)).when(service).deleteById(id);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /products/{id}/activate e /deactivate devem retornar 204")
    void activateAndDeactivateShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/products/{id}/activate", id)).andExpect(status().isNoContent());
        mockMvc.perform(patch("/products/{id}/deactivate", id)).andExpect(status().isNoContent());

        verify(service).turnOn(id);
        verify(service).turnOff(id);
    }
}
