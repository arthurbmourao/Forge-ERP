package forge.erp.product.model;

import forge.erp.category.model.Category;
import forge.erp.unit.model.Unit;
import jakarta.persistence.*;
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
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_unit_id", nullable = false)
    private Unit stockUnit;

    @Column(precision = 19, scale = 6)
    private BigDecimal weight;

    @Column(precision = 19, scale = 6)
    private BigDecimal minimumStock;

    @Column(precision = 19, scale = 6)
    private BigDecimal maximumStock;

    private boolean active;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Product(
            String code,
            String name,
            String description,
            Category category,
            Unit stockUnit,
            BigDecimal weight,
            BigDecimal minimumStock,
            BigDecimal maximumStock,
            boolean active
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.stockUnit = stockUnit;
        this.weight = weight;
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
        this.active = active;
    }
}
