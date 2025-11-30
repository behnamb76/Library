package ir.bahman.library.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
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
}
