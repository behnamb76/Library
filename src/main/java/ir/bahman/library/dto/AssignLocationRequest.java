package ir.bahman.library.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignLocationRequest {
    private Long bookCopyId;

    private Long locationId;
}
