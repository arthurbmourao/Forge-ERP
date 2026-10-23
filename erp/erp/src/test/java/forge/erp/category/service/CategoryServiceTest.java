package forge.erp.category.service;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.mapper.CategoryMapper;
import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper mapper;

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    private Category sampleEntity() {
        Category category = new Category("MOT-001", "Motores", "Categoria de motores");
        category.setId(UUID.randomUUID());
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private CategoryResponse responseOf(Category c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getDescription(),
                c.getActive(), c.getCreatedAt(), c.getUpdatedAt());
    }

    @Test
    @DisplayName("saveCategory deve persistir e retornar a resposta mapeada")
    void saveCategoryShouldPersistAndReturnResponse() {
        CategoryRequest request = new CategoryRequest("MOT-001", "Motores", "Categoria de motores");
        Category entity = sampleEntity();
        CategoryResponse expected = responseOf(entity);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(expected);

        CategoryResponse result = service.saveCategory(request);

        assertThat(result).isEqualTo(expected);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("findById deve retornar categoria existente")
    void findByIdShouldReturnExisting() {
        Category entity = sampleEntity();
        CategoryResponse expected = responseOf(entity);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        CategoryResponse result = service.findById(entity.getId());

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("findById deve lancar excecao quando nao encontrado")
    void findByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findAll deve mapear todas as categorias")
    void findAllShouldMapAll() {
        Category entity = sampleEntity();
        CategoryResponse expected = responseOf(entity);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        List<CategoryResponse> result = service.findAll();

        assertThat(result).containsExactly(expected);
    }

    @Test
    @DisplayName("update deve aplicar os valores do request na entidade e persistir")
    void updateShouldApplyRequestValues() {
        Category entity = sampleEntity();
        UUID id = entity.getId();
        CategoryRequest request = new CategoryRequest("MOT-999", "Motores Eletricos", "Nova descricao");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Category.class))).thenReturn(entity);
        when(mapper.toResponse(any(Category.class))).thenReturn(responseOf(entity));

        service.update(id, request);

        // regressao: valores do request devem ser aplicados, nao os antigos
        assertThat(entity.getCode()).isEqualTo("MOT-999");
        assertThat(entity.getName()).isEqualTo("Motores Eletricos");
        assertThat(entity.getDescription()).isEqualTo("Nova descricao");
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update deve lancar excecao quando categoria nao existe")
    void updateShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        CategoryRequest request = new CategoryRequest("X", "Y", "Z");
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(RuntimeException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById deve delegar para o repositorio")
    void deleteByIdShouldDelegate() {
        UUID id = UUID.randomUUID();

        service.deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("turnOn deve ativar a categoria")
    void turnOnShouldActivate() {
        Category entity = sampleEntity();
        entity.setActive(false);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOn(entity.getId());

        assertThat(entity.getActive()).isTrue();
    }

    @Test
    @DisplayName("turnOff deve desativar a categoria")
    void turnOffShouldDeactivate() {
        Category entity = sampleEntity();
        entity.setActive(true);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOff(entity.getId());

        assertThat(entity.getActive()).isFalse();
    }
}
