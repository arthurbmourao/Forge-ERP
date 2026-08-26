package forge.erp.unit.controller;

import forge.erp.unit.DTO.UnitDtoRequest;
import forge.erp.unit.DTO.UnitDtoResponse;
import forge.erp.unit.model.Unit;
import forge.erp.unit.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/unit")
public class UnitController {

    private final UnitService service;

    @PostMapping
    public ResponseEntity<UnitDtoResponse> saveUnit(@RequestBody UnitDtoRequest unitDtoRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveUnit(unitDtoRequest));
    }

    @GetMapping
    public ResponseEntity<List<UnitDtoResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(service.getAll());
    }

}
