package forge.erp.unitconversion.controller;

import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.service.UnitConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/unit-conversion")
public class UnitConversionController {

    private final UnitConversionService service;

    @PostMapping
    @Valid
    public ResponseEntity<UnitConversionResponse> saveUnitConversion (@RequestBody UnitConversionRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveUnitConversion(request));
    }

    @GetMapping
    public ResponseEntity<List<UnitConversionResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @GetMapping("/{id}")
    @Valid
    public ResponseEntity<UnitConversionResponse> findBYId(@RequestParam UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(service.findById(id));
    }

    @PutMapping("/{id}")
    @Valid
    public ResponseEntity<UnitConversionResponse> updateUnitConversion(@RequestParam UUID id, @RequestBody UnitConversionRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(service.update(request, id));
    }

    @PutMapping("/{id}/activate")
    @Valid
    public ResponseEntity<Void> setActivate(@RequestParam UUID id){
        service.turnOn(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desactivate")
    @Valid
    public ResponseEntity<Void> setDeactivate(@RequestParam UUID id){
        service.turnOff(id);
        return ResponseEntity.noContent().build();
    }
}
