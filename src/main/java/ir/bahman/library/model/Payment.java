package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.model.enums.PaymentFor;
import ir.bahman.library.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity<Long> {
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentFor paymentFor;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Person member;

    @OneToOne(mappedBy = "payment")
    private Penalty penalty;
}
