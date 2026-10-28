package forge.erp.unitconversion.mapper;

import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.model.UnitConversion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitConversionMapperTest {

    private UnitConversionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UnitConversionMapper();
    }

    private Unit unit(String code, String name, String symbol) {
        Unit unit = new Unit(code, name, symbol, UnitType.MASS, 3, true);
        unit.setId(UUID.randomUUID());
        return unit;
    }

    @Test
    @DisplayName("toResponse deve mapear todos os campos da entidade")
    void toResponseShouldMapEntityFields() {
        Unit from = unit("KG", "Quilograma", "kg");
        Unit to = unit("G", "Grama", "g");
        LocalDateTime now = LocalDateTime.now();

        UnitConversion conversion = new UnitConversion();
        conversion.setId(UUID.randomUUID());
        conversion.setFromUnit(from);
        conversion.setToUnit(to);
        conversion.setFactor(new BigDecimal("1000"));
        conversion.setActive(true);
        conversion.setCreatedAt(now);

        UnitConversionResponse response = mapper.toResponse(conversion);

        assertThat(response.id()).isEqualTo(conversion.getId());
        assertThat(response.fromUnit()).isEqualTo(from);
        assertThat(response.toUnit()).isEqualTo(to);
        assertThat(response.factor()).isEqualByComparingTo("1000");
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toEntity deve usar as unidades resolvidas e os valores do request")
    void toEntityShouldMapRequestFields() {
        Unit from = unit("KG", "Quilograma", "kg");
        Unit to = unit("G", "Grama", "g");
        UnitConversionRequest request =
                new UnitConversionRequest(from.getId(), to.getId(), new BigDecimal("1000"), true);

        UnitConversion entity = mapper.toEntity(request, from, to);

        assertThat(entity.getFromUnit()).isSameAs(from);
        assertThat(entity.getToUnit()).isSameAs(to);
        assertThat(entity.getFactor()).isEqualByComparingTo("1000");
        assertThat(entity.isActive()).isTrue();
    }
}
