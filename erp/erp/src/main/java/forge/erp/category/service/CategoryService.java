package forge.erp.category.service;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.mapper.CategoryMapper;
import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper mapper;
    private final CategoryRepository repository;

    public CategoryResponse saveCategory(CategoryRequest categoryRequest){
        Category category = mapper.toEntity(categoryRequest);

        category = repository.save(category);

        return mapper.toResponse(category);
    }

    public CategoryResponse findById (UUID id){
        Category category = repository.findById(id).orElseThrow();

        return mapper.toResponse(category);

    }

    public List<CategoryResponse> findAll(){
        List<Category> categories = repository.findAll();

        return categories.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest categoryRequest){
        Category category = repository.findById(id).orElseThrow(RuntimeException::new);

        category.setName(categoryRequest.name());
        category.setCode(categoryRequest.code());
        category.setDescription(categoryRequest.description());
        category.setUpdatedAt(java.time.LocalDateTime.now());

        return mapper.toResponse(category);

    }

    public void deleteById(UUID id){
        repository.deleteById(id);
    }

    @Transactional
    public void turnOn(UUID id){
        Category category = repository.findById(id).orElseThrow();

        category.setActive(true);
    }

    @Transactional
    public void turnOff(UUID id){
        Category category = repository.findById(id).orElseThrow();

        category.setActive(false);
    }
}
