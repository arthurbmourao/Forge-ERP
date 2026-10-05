package forge.erp.category;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.mapper.CategoryMapper;
import forge.erp.category.model.Category;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void mapsRequestCodeAndNameToTheirMatchingFields() {
        Category category = mapper.toEntity(new CategoryRequest("FASTENER", "Fixadores", "Peças de fixação"));

        assertEquals("FASTENER", category.getCode());
        assertEquals("Fixadores", category.getName());
        assertEquals("Peças de fixação", category.getDescription());
        assertTrue(category.getActive());
    }

    @Test
    void mapsEntityFieldsToResponseInTheDeclaredOrder() {
        Category category = new Category("FASTENER", "Fixadores", "Peças de fixação");
        UUID id = UUID.randomUUID();
        category.setId(id);

        CategoryResponse response = mapper.toResponse(category);

        assertEquals(id, response.id());
        assertEquals("FASTENER", response.code());
        assertEquals("Fixadores", response.name());
        assertEquals("Peças de fixação", response.description());
        assertTrue(response.active());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }
}
