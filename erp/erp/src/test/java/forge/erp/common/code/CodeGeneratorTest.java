package forge.erp.common.code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeGeneratorTest {

    private CodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CodeGenerator();
    }

    @Test
    @DisplayName("deve gerar codigo no formato PREFIXO-XXXX com 4 digitos")
    void shouldGenerateCodeInExpectedFormat() {
        String code = generator.generate("CAT", c -> false);

        assertThat(code).matches("CAT-\\d{4}");
    }

    @Test
    @DisplayName("deve respeitar o prefixo informado")
    void shouldUseGivenPrefix() {
        assertThat(generator.generate("UNI", c -> false)).startsWith("UNI-");
        assertThat(generator.generate("CAT", c -> false)).startsWith("CAT-");
    }

    @Test
    @DisplayName("deve retornar um codigo que ainda nao existe")
    void shouldReturnCodeThatDoesNotExist() {
        Set<String> existentes = new HashSet<>();
        existentes.add("CAT-0001");

        String code = generator.generate("CAT", existentes::contains);

        assertThat(existentes).doesNotContain(code);
    }

    @Test
    @DisplayName("deve tentar novamente quando ocorre colisao ate achar um livre")
    void shouldRetryOnCollision() {
        // predicado que considera existente nas 3 primeiras consultas e livre depois
        Predicate<String> existsFn = new Predicate<>() {
            int calls = 0;

            @Override
            public boolean test(String code) {
                calls++;
                return calls <= 3;
            }
        };

        String code = generator.generate("CAT", existsFn);

        assertThat(code).matches("CAT-\\d{4}");
    }

    @Test
    @DisplayName("deve lancar excecao quando todos os codigos colidem")
    void shouldThrowWhenAlwaysColliding() {
        assertThatThrownBy(() -> generator.generate("CAT", c -> true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CAT");
    }
}
