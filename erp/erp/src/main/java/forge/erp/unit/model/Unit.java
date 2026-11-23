package forge.erp.unit.model;

import forge.erp.unit.enums.UnitType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "units")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    private String code;

    private String name;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private UnitType type;

    private Integer decimalPlaces;

    private boolean active;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();



    public Unit(
            String code,
            String name,
            String symbol,
            UnitType type,
            Integer decimalPlaces,
            boolean active
    ) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.type = type;
        this.decimalPlaces = decimalPlaces;
        this.active = active;
    }


}
