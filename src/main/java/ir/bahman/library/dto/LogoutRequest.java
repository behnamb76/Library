package ir.bahman.library.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    private String refreshToken;
}
