package forge.erp.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse (
        UUID id,

        String code,

        String name,

        String description,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
){
}
