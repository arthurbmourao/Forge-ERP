package forge.erp.unitconversion.model;

import forge.erp.unit.model.Unit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "unit_conversions")
public class UnitConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "from_unit_id", nullable = false)
    private Unit fromUnit;

    @ManyToOne()
    @JoinColumn(name = "to_unit_id", nullable = false)
    private Unit toUnit;

    private BigDecimal factor;

    private boolean active;

    private LocalDateTime createdAt = LocalDateTime.now();

    public UnitConversion(@NotNull Unit fromUnit, @NotNull Unit toUnit, @NotNull BigDecimal factor, @NotNull boolean active) {
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.factor = factor;
        this.active = active;

    }
}
