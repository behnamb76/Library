package ir.bahman.library.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private LocalDateTime reserveDate;

    private LocalDateTime expireDate;

    private Integer queuePosition;

    private Long memberId;

    private Long bookId;
}
