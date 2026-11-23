package forge.erp.productunit.controller;

import forge.erp.productunit.dto.ProductUnitRequest;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.service.ProductUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

// as unidades do produto fazem parte do recurso Product: usam as permissoes PRODUCT_*
@RestController
@RequiredArgsConstructor
@RequestMapping("/products/{productId}/units")
public class ProductUnitController {

    private final ProductUnitService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductUnitResponse> create(@PathVariable UUID productId,
                                                      @Valid @RequestBody ProductUnitRequest request) {
        ProductUnitResponse response = service.save(productId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductUnitResponse>> findAll(@PathVariable UUID productId) {
        return ResponseEntity.ok(service.findAll(productId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductUnitResponse> findById(@PathVariable UUID productId,
                                                        @PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(productId, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductUnitResponse> update(@PathVariable UUID productId,
                                                      @PathVariable UUID id,
                                                      @Valid @RequestBody ProductUnitRequest request) {
        return ResponseEntity.ok(service.update(productId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable UUID productId, @PathVariable UUID id) {
        service.deleteById(productId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Void> activate(@PathVariable UUID productId, @PathVariable UUID id) {
        service.turnOn(productId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID productId, @PathVariable UUID id) {
        service.turnOff(productId, id);
        return ResponseEntity.noContent().build();
    }
}
