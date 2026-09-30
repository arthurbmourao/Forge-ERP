package forge.erp.category.mapper;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public Category toEntity(CategoryRequest categoryRequest){

        return new Category(
                categoryRequest.name(),
                categoryRequest.name(),
                categoryRequest.description()
        );
    }
}
