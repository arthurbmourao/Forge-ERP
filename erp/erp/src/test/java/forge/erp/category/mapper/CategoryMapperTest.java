package forge.erp.category.mapper;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private CategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoryMapper();
    }

    @Test
    @DisplayName("toEntity deve copiar name e description do request e nao definir code")
    void toEntityShouldMapRequestFields() {
        CategoryRequest request = new CategoryRequest("Motores", "Categoria de motores");

        Category entity = mapper.toEntity(request);

        // code e gerado no servidor, nao vem do request
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getName()).isEqualTo("Motores");
        assertThat(entity.getDescription()).isEqualTo("Categoria de motores");
    }

    @Test
    @DisplayName("toResponse deve mapear code e name nas posicoes corretas")
    void toResponseShouldMapCodeAndNameToCorrectFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Category category = new Category("PAR-001", "Parafusos", "Categoria de parafusos");
        category.setId(id);
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.id()).isEqualTo(id);
        // regressao: code deve conter o code (nao o name)
        assertThat(response.code()).isEqualTo("PAR-001");
        assertThat(response.name()).isEqualTo("Parafusos");
        assertThat(response.description()).isEqualTo("Categoria de parafusos");
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
