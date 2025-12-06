package ir.bahman.library.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required!")
    private String oldPassword;

    @NotBlank(message = "New password is required!")
    @Pattern.List({
            @Pattern(regexp = ".{8,}", message = "Password must be at least 8 characters"),
            @Pattern(regexp = ".*[0-9].*", message = "Password must contain a digit"),
            @Pattern(regexp = ".*[a-zA-Z].*", message = "Password must contain a letter")
    })
    private String newPassword;

    @AssertTrue(message = "The new password must be different from the current password.")
    public boolean isNewPasswordDifferent() {
        if (oldPassword == null || newPassword == null)
            return true;
        return !newPassword.equals(oldPassword);
    }
}
