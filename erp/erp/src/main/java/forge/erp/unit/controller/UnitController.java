package forge.erp.unit.controller;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/unit")
public class UnitController {

    private final UnitService service;

    @PostMapping
    @Valid
    public ResponseEntity<UnitDtoResponse> saveUnit(@RequestBody UnitDtoRequest unitDtoRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveUnit(unitDtoRequest));
    }

    @GetMapping
    @Valid
    public ResponseEntity<List<UnitDtoResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.getAll());
    }

    @GetMapping("/{id}")
    @Valid
    public ResponseEntity<UnitDtoResponse> findById(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(service.getById(id));
    }

    @DeleteMapping("/{id}")
    @Valid
    public ResponseEntity<Void> deleteById(@PathVariable UUID id){
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
