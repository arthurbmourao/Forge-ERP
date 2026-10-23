package forge.erp.product;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Placeholder de testes para a futura entidade Product.
 *
 * A entidade Product ainda nao existe no projeto. Conforme a documentacao (item 10),
 * Product tera: id, sku (unico), name, description, category (N:1 Category),
 * stockUnit (N:1 Unit), weight, minimumStock, maximumStock, active, createdAt, updatedAt.
 *
 * Quando Product/ProductMapper/ProductService forem criados, siga o mesmo padrao usado
 * em Category/Unit:
 *   - ProductMapperTest  -> instanciar o mapper diretamente, validar toEntity/toResponse
 *   - ProductServiceTest -> @ExtendWith(MockitoExtension.class) com @Mock repository/mapper
 *                           e @InjectMocks service; cobrir save/findById/findAll/update/
 *                           delete/turnOn/turnOff.
 *
 * Casos de teste sugeridos para a regra de negocio especifica de Product:
 *   - save deve exigir category e stockUnit (obrigatorios);
 *   - sku deve ser unico (rejeitar duplicados);
 *   - weight/minimumStock/maximumStock sao opcionais (podem ser null).
 *
 * Este teste esta desabilitado para nao quebrar o build enquanto a entidade nao existe.
 * Remova o @Disabled e implemente os casos acima apos criar Product.
 */
@Disabled("Entidade Product ainda nao implementada - habilitar apos a criacao de Product")
class ProductPlaceholderTest {

    @Test
    void productTestsPendingImplementation() {
        // Implementar apos a criacao da entidade Product.
    }
}
