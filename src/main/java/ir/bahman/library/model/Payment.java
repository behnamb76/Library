package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.model.enums.PaymentMethod;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity<Long> {
    private Double amount;
    private LocalDateTime date;
    private PaymentMethod method;
    private
}
