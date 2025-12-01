package ir.bahman.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Refresh token is required!")
    private String refreshToken;
}
