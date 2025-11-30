package ir.bahman.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username is required!")
    private String username;

    @NotBlank(message = "Username is required!")
    private String password;
}
