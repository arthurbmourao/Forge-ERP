package forge.erp.unit;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.mapper.UnitMapper;
import forge.erp.unit.model.Unit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitMapperTest {

    private final UnitMapper mapper = new UnitMapper();

    @Test
    void mapsRequestToEntityWithoutSwappingCodeAndName() {
        UnitDtoRequest request = new UnitDtoRequest("Kilogram", "KG", "kg", UnitType.MASS, 3, true);

        Unit unit = mapper.toEntity(request);

        assertEquals("KG", unit.getCode());
        assertEquals("Kilogram", unit.getName());
        assertEquals(UnitType.MASS, unit.getType());
        assertEquals(3, unit.getDecimalPlaces());
        assertTrue(unit.isActive());
    }

    @Test
    void mapsEntityToResponseIncludingIdAndDates() {
        Unit unit = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, true);
        UUID id = UUID.randomUUID();
        unit.setId(id);

        UnitDtoResponse response = mapper.toResponse(unit);

        assertEquals(id, response.id());
        assertEquals("KG", response.code());
        assertEquals("Kilogram", response.name());
        assertEquals("kg", response.symbol());
        assertNotNull(response.createdAt());
        assertNotNull(response.updateAt());
    }
}
