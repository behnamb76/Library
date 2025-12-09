package ir.bahman.library.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    @NotBlank(message = "Section is required")
    @Size(max = 50, message = "Section must not exceed 50 characters")
    private String section;

    @NotBlank(message = "Shelf is required")
    @Size(max = 50, message = "Shelf must not exceed 50 characters")
    private String shelf;

    @NotNull(message = "Row is required")
    @Min(value = 1, message = "Row must be at least 1")
    @Max(value = 100, message = "Row must not exceed 100")
    private Integer row;
}
