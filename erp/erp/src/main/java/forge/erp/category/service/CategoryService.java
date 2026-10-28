package forge.erp.category.service;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.mapper.CategoryMapper;
import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import forge.erp.common.code.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String CODE_PREFIX = "CAT";

    private final CategoryMapper mapper;
    private final CategoryRepository repository;
    private final CodeGenerator codeGenerator;

    public CategoryResponse saveCategory(CategoryRequest categoryRequest){
        Category category = mapper.toEntity(categoryRequest);

        category.setCode(codeGenerator.generate(CODE_PREFIX, repository::existsByCode));

        category = repository.save(category);

        return mapper.toResponse(category);
    }

    public CategoryResponse findById (UUID id){
        Category category = repository.findById(id).orElseThrow(RuntimeException::new);

        return mapper.toResponse(category);

    }

    public List<CategoryResponse> findAll(){
        List<Category> categories = repository.findAll();

        return categories.stream().map(mapper::toResponse).toList();
    }

    public CategoryResponse update(UUID id, CategoryRequest categoryRequest){
        Category category = repository.findById(id).orElseThrow(RuntimeException::new);


        category.setName(categoryRequest.name());
        category.setDescription(categoryRequest.description());
        category.setUpdatedAt(LocalDateTime.now());

        category = repository.save(category);

        return mapper.toResponse(category);

    }

    public void deleteById(UUID id){
        repository.deleteById(id);
    }

    public void turnOn(UUID id){
        Category category = repository.findById(id).orElseThrow(RuntimeException::new);

        category.setActive(true);
    }

    public void turnOff(UUID id){
        Category category = repository.findById(id).orElseThrow(RuntimeException::new);

        category.setActive(false);
    }


}
