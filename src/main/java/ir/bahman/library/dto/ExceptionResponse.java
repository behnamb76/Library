package ir.bahman.library.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
