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
    public ResponseEntity<CategoryResponse> saveCategory (@Valid @RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCategory(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @PutMapping
    public ResponseEntity<CategoryResponse> updateCategory(@RequestParam UUID id, @Valid @RequestBody CategoryRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(service.update(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id){
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> setActivate(@PathVariable UUID id){
        service.turnOn(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desactivate")
    public ResponseEntity<Void> setDeactivate(@PathVariable UUID id){
        service.turnOff(id);
        return ResponseEntity.noContent().build();
    }


}
