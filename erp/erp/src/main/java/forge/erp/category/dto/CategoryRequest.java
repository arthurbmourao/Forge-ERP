package forge.erp.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @Size(max = 50)
        @NotBlank
        String code,

        @Size(max = 100)
        @NotBlank
        String name,


        @NotBlank
        String description
) {
}
