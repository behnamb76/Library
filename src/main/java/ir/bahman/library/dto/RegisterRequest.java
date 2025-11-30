package ir.bahman.library.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "First name is required!")
    @Size(max = 50, message = "firstname must be {max} characters maximum!")
    private String firstName;

    @NotBlank(message = "Last name is required!")
    @Size(max = 50, message = "lastname must be {max} characters maximum!")
    private String lastName;

    @NotBlank(message = "National code is required!")
    @Pattern(regexp = "\\d{10}", message = "National code must be exactly 10 digits with no whitespace")
    private String nationalCode;

    @NotBlank(message = "Phone number is required!")
    @Pattern(regexp = "09\\d{9}", message = "Phone number must be 11 digits starting with 09 (e.g., 09123456789)")
    private String phoneNumber;

    @Past(message = "Birthday must be in the past")
    @NotNull(message = "Birthday is required!")
    private LocalDate birthday;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Invalid username format")
    private String username;

    @NotBlank(message = "Password is required!")
    @Pattern.List({
            @Pattern(regexp = ".{8,}", message = "Password must be at least 8 characters"),
            @Pattern(regexp = ".*[0-9].*", message = "Password must contain a digit"),
            @Pattern(regexp = ".*[a-zA-Z].*", message = "Password must contain a letter")
    })
    private String password;
}
