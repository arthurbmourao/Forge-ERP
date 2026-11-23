package forge.erp.config;

import forge.erp.category.controller.CategoryController;
import forge.erp.category.service.CategoryService;
import forge.erp.common.exception.GlobalExceptionHandler;
import forge.erp.product.controller.ProductController;
import forge.erp.product.dto.ProductResponse;
import forge.erp.product.service.ProductService;
import forge.erp.productunit.controller.ProductUnitController;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.service.ProductUnitService;
import forge.erp.unit.controller.UnitController;
import forge.erp.unit.service.UnitService;
import forge.erp.unitconversion.controller.UnitConversionController;
import forge.erp.unitconversion.service.UnitConversionService;
import forge.erp.user.controller.UserController;
import forge.erp.user.service.UserManagementService;
import forge.erp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica, com o SecurityConfig real, que cada endpoint exige exatamente a permissao
 * RECURSO_READ (GET), RECURSO_CREATE (POST), RECURSO_UPDATE (PUT/PATCH) ou RECURSO_DELETE (DELETE).
 */
@WebMvcTest(controllers = {
        CategoryController.class,
        UnitController.class,
        UnitConversionController.class,
        ProductController.class,
        ProductUnitController.class,
        UserController.class
})
@Import({SecurityConfig.class, LocalUserJwtAuthenticationConverter.class, GlobalExceptionHandler.class})
class SecurityAuthorizationTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // as 18 permissoes do sistema (mesmas da migration V7)
    private static final List<String> ALL_PERMISSIONS = List.of(
            "CATEGORY_READ", "CATEGORY_CREATE", "CATEGORY_UPDATE", "CATEGORY_DELETE",
            "UNIT_READ", "UNIT_CREATE", "UNIT_UPDATE", "UNIT_DELETE",
            "UNIT_CONVERSION_READ", "UNIT_CONVERSION_CREATE", "UNIT_CONVERSION_UPDATE", "UNIT_CONVERSION_DELETE",
            "PRODUCT_READ", "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE",
            "USER_READ", "USER_UPDATE");

    private static final String CATEGORY_BODY = "{\"name\":\"Fixadores\",\"description\":\"Parafusos\"}";
    private static final String UNIT_BODY = "{\"name\":\"Quilograma\",\"symbol\":\"kg\",\"type\":\"MASS\","
            + "\"decimalPlaces\":3,\"active\":true}";
    private static final String CONVERSION_BODY = "{\"fromUnitId\":\"" + ID + "\",\"toUnitId\":\"" + OTHER_ID
            + "\",\"factor\":1000,\"active\":true}";
    private static final String PRODUCT_BODY = "{\"name\":\"Parafuso\",\"description\":\"d\",\"categoryId\":\"" + ID
            + "\",\"stockUnitId\":\"" + OTHER_ID + "\",\"weight\":1,\"minimumStock\":1,\"maximumStock\":2,"
            + "\"active\":true}";
    private static final String PRODUCT_UNIT_BODY = "{\"unitId\":\"" + ID + "\",\"conversionFactor\":100,"
            + "\"purchaseUnit\":true,\"issueUnit\":true,\"active\":true}";
    private static final String USER_ROLE_BODY = "{\"role\":\"VIEWER\"}";

    @Autowired
    private MockMvc mockMvc;

    // dependencias dos controllers e da autenticacao (a seguranca e a parte real sob teste)
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UnitService unitService;
    @MockitoBean
    private UnitConversionService unitConversionService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private ProductUnitService productUnitService;
    @MockitoBean
    private UserManagementService userManagementService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    record Endpoint(HttpMethod method, String path, String authority, String body) {
        @Override
        public String toString() {
            return method + " " + path + " -> " + authority;
        }
    }

    static Stream<Endpoint> endpoints() {
        return Stream.of(
                // Category
                new Endpoint(HttpMethod.GET, "/category", "CATEGORY_READ", null),
                new Endpoint(HttpMethod.GET, "/category/" + ID, "CATEGORY_READ", null),
                new Endpoint(HttpMethod.POST, "/category", "CATEGORY_CREATE", CATEGORY_BODY),
                new Endpoint(HttpMethod.PUT, "/category?id=" + ID, "CATEGORY_UPDATE", CATEGORY_BODY),
                new Endpoint(HttpMethod.DELETE, "/category/" + ID, "CATEGORY_DELETE", null),
                new Endpoint(HttpMethod.PUT, "/category/" + ID + "/activate", "CATEGORY_UPDATE", null),
                new Endpoint(HttpMethod.PUT, "/category/" + ID + "/desactivate", "CATEGORY_UPDATE", null),
                // Unit
                new Endpoint(HttpMethod.GET, "/unit", "UNIT_READ", null),
                new Endpoint(HttpMethod.GET, "/unit/" + ID, "UNIT_READ", null),
                new Endpoint(HttpMethod.POST, "/unit", "UNIT_CREATE", UNIT_BODY),
                new Endpoint(HttpMethod.DELETE, "/unit/" + ID, "UNIT_DELETE", null),
                new Endpoint(HttpMethod.PUT, "/unit/" + ID + "/activate", "UNIT_UPDATE", null),
                new Endpoint(HttpMethod.PUT, "/unit/" + ID + "/desactivate", "UNIT_UPDATE", null),
                // UnitConversion
                new Endpoint(HttpMethod.GET, "/unit-conversion", "UNIT_CONVERSION_READ", null),
                new Endpoint(HttpMethod.GET, "/unit-conversion/" + ID, "UNIT_CONVERSION_READ", null),
                new Endpoint(HttpMethod.POST, "/unit-conversion", "UNIT_CONVERSION_CREATE", CONVERSION_BODY),
                new Endpoint(HttpMethod.PUT, "/unit-conversion/" + ID, "UNIT_CONVERSION_UPDATE", CONVERSION_BODY),
                new Endpoint(HttpMethod.PUT, "/unit-conversion/" + ID + "/activate", "UNIT_CONVERSION_UPDATE", null),
                new Endpoint(HttpMethod.PUT, "/unit-conversion/" + ID + "/desactivate", "UNIT_CONVERSION_UPDATE", null),
                // Product
                new Endpoint(HttpMethod.GET, "/products", "PRODUCT_READ", null),
                new Endpoint(HttpMethod.GET, "/products/" + ID, "PRODUCT_READ", null),
                new Endpoint(HttpMethod.POST, "/products", "PRODUCT_CREATE", PRODUCT_BODY),
                new Endpoint(HttpMethod.PUT, "/products/" + ID, "PRODUCT_UPDATE", PRODUCT_BODY),
                new Endpoint(HttpMethod.DELETE, "/products/" + ID, "PRODUCT_DELETE", null),
                new Endpoint(HttpMethod.PATCH, "/products/" + ID + "/activate", "PRODUCT_UPDATE", null),
                new Endpoint(HttpMethod.PATCH, "/products/" + ID + "/deactivate", "PRODUCT_UPDATE", null),
                // ProductUnit (usa as permissoes PRODUCT_*)
                new Endpoint(HttpMethod.GET, "/products/" + ID + "/units", "PRODUCT_READ", null),
                new Endpoint(HttpMethod.GET, "/products/" + ID + "/units/" + OTHER_ID, "PRODUCT_READ", null),
                new Endpoint(HttpMethod.POST, "/products/" + ID + "/units", "PRODUCT_CREATE", PRODUCT_UNIT_BODY),
                new Endpoint(HttpMethod.PUT, "/products/" + ID + "/units/" + OTHER_ID, "PRODUCT_UPDATE", PRODUCT_UNIT_BODY),
                new Endpoint(HttpMethod.DELETE, "/products/" + ID + "/units/" + OTHER_ID, "PRODUCT_DELETE", null),
                new Endpoint(HttpMethod.PATCH, "/products/" + ID + "/units/" + OTHER_ID + "/activate", "PRODUCT_UPDATE", null),
                new Endpoint(HttpMethod.PATCH, "/products/" + ID + "/units/" + OTHER_ID + "/deactivate", "PRODUCT_UPDATE", null),
                // User (gestao de usuarios)
                new Endpoint(HttpMethod.GET, "/users", "USER_READ", null),
                new Endpoint(HttpMethod.GET, "/users/" + ID, "USER_READ", null),
                new Endpoint(HttpMethod.PUT, "/users/" + ID + "/role", "USER_UPDATE", USER_ROLE_BODY),
                new Endpoint(HttpMethod.PATCH, "/users/" + ID + "/activate", "USER_UPDATE", null),
                new Endpoint(HttpMethod.PATCH, "/users/" + ID + "/deactivate", "USER_UPDATE", null)
        );
    }

    private MockHttpServletRequestBuilder request(Endpoint endpoint) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(endpoint.method(), endpoint.path());
        if (endpoint.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        return builder;
    }

    private List<GrantedAuthority> authorities(List<String> names) {
        return names.stream().<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();
    }

    private void stubServicesThatBuildTheResponseFromTheResult() {
        // os controllers de criacao leem o id do resultado para montar o header Location
        when(productService.saveProduct(any())).thenReturn(new ProductResponse(ID, "PRD-0001", "Parafuso", "d",
                ID, "Fixadores", OTHER_ID, "UN", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN, true,
                LocalDateTime.now(), LocalDateTime.now()));
        when(productUnitService.save(any(), any())).thenReturn(new ProductUnitResponse(ID, ID, OTHER_ID,
                "UNI-0002", "CX", BigDecimal.TEN, true, true, true));
    }

    @Test
    @DisplayName("a lista de permissoes do teste cobre exatamente as exigidas pelos endpoints")
    void endpointsShouldUseOnlyKnownPermissionsAndCoverAllOfThem() {
        List<String> used = endpoints().map(Endpoint::authority).distinct().sorted().toList();

        assertThat(ALL_PERMISSIONS).containsAll(used);
        // UNIT_CONVERSION_DELETE existe como permissao, mas ainda nao ha endpoint de remocao
        assertThat(ALL_PERMISSIONS).hasSize(18);
        assertThat(used).containsExactlyInAnyOrderElementsOf(
                ALL_PERMISSIONS.stream().filter(p -> !p.equals("UNIT_CONVERSION_DELETE")).toList());
    }

    @ParameterizedTest(name = "sem token: {0} deve retornar 401")
    @MethodSource("endpoints")
    void withoutTokenShouldReturn401(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "sem a permissao exata: {0} deve retornar 403")
    @MethodSource("endpoints")
    void withoutRequiredPermissionShouldReturn403(Endpoint endpoint) throws Exception {
        List<String> everythingButRequired = ALL_PERMISSIONS.stream()
                .filter(permission -> !permission.equals(endpoint.authority()))
                .toList();

        mockMvc.perform(request(endpoint).with(jwt().authorities(authorities(everythingButRequired))))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "com a permissao: {0} deve ser liberado")
    @MethodSource("endpoints")
    void withRequiredPermissionShouldBeAllowed(Endpoint endpoint) throws Exception {
        stubServicesThatBuildTheResponseFromTheResult();

        int status = mockMvc.perform(request(endpoint).with(jwt().authorities(authorities(List.of(endpoint.authority())))))
                .andReturn().getResponse().getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @DisplayName("usuario inativo (sem nenhuma authority) deve receber 403")
    void userWithoutAuthoritiesShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/category").with(jwt().authorities(List.of())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("quem so le (VIEWER) nao enxerga nem altera usuarios")
    void viewerPermissionsShouldNotGrantUserManagement() throws Exception {
        List<GrantedAuthority> viewer = authorities(List.of(
                "CATEGORY_READ", "UNIT_READ", "UNIT_CONVERSION_READ", "PRODUCT_READ"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users").with(jwt().authorities(viewer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + ID + "/deactivate").with(jwt().authorities(viewer)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("quem tem todas as permissoes de negocio mas nao as de usuario (ANALISTA) nao gerencia usuarios")
    void analistaPermissionsShouldNotGrantUserManagement() throws Exception {
        List<GrantedAuthority> analista = authorities(
                ALL_PERMISSIONS.stream().filter(p -> !p.startsWith("USER_")).toList());

        mockMvc.perform(MockMvcRequestBuilders.get("/users").with(jwt().authorities(analista)))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.put("/users/" + ID + "/role")
                        .contentType(MediaType.APPLICATION_JSON).content(USER_ROLE_BODY)
                        .with(jwt().authorities(analista)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("quem tem todas as 18 permissoes (ADMIN) acessa todos os endpoints")
    void allPermissionsShouldAllowEveryEndpoint() throws Exception {
        stubServicesThatBuildTheResponseFromTheResult();
        List<GrantedAuthority> adminAuthorities = authorities(ALL_PERMISSIONS);

        for (Endpoint endpoint : endpoints().toList()) {
            int status = mockMvc.perform(request(endpoint).with(jwt().authorities(adminAuthorities)))
                    .andReturn().getResponse().getStatus();
            assertThat(status).as(endpoint.toString()).isNotIn(401, 403);
        }
    }

    @Test
    @DisplayName("Swagger, api-docs e health nao exigem token")
    void publicPathsShouldNotRequireToken() throws Exception {
        for (String path : List.of("/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs", "/actuator/health")) {
            int status = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn().getResponse().getStatus();
            assertThat(status).as(path).isNotIn(401, 403);
        }
    }

    @Test
    @DisplayName("rota fora da lista publica continua exigindo token")
    void otherPathsShouldRequireToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/qualquer-coisa"))
                .andExpect(status().isUnauthorized());
    }
}
