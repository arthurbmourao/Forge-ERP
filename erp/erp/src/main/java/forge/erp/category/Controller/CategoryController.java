package forge.erp.category.Controller;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.model.Category;
import forge.erp.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryResponse> saveCategory (@RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCategory(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@RequestParam UUID id){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @PutMapping
    public ResponseEntity<CategoryResponse> updateCategory(@RequestParam UUID id, @RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(service.update(id,request));
    }
}
