package forge.erp.category.controller;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.service.CategoryService;
import jakarta.validation.Valid;
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
    @Valid
    public ResponseEntity<CategoryResponse> saveCategory (@RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCategory(request));
    }

    @GetMapping("/{id}")
    @Valid
    public ResponseEntity<CategoryResponse> findById(@RequestParam UUID id){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @PutMapping
    @Valid
    public ResponseEntity<CategoryResponse> updateCategory(@RequestParam UUID id, @RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(service.update(id,request));
    }

    @DeleteMapping("/{id}")
    @Valid
    public ResponseEntity<Void> deleteCategory(@RequestParam UUID id){
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> setActivate(@RequestParam UUID id){
        service.turnOn(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desactivate")
    public ResponseEntity<Void> setDeactivate(@RequestParam UUID id){
        service.turnOff(id);
        return ResponseEntity.noContent().build();
    }


}
