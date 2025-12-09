package ir.bahman.library.mapper;

import ir.bahman.library.dto.PaymentDTO;
import ir.bahman.library.model.Payment;
import ir.bahman.library.model.enums.PaymentFor;
import ir.bahman.library.model.enums.PaymentMethod;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class PaymentMapper implements BaseMapper<Payment, PaymentDTO> {
    public abstract Payment toEntity(PaymentDTO dto);

    public abstract PaymentDTO toDto(Payment payment);

    @AfterMapping
    protected void afterToDto(Payment payment, @MappingTarget PaymentDTO dto) {
        dto.setMethod(payment.getMethod().toString());
        dto.setPaymentFor(payment.getPaymentFor().toString());
    }

    @AfterMapping
    protected void afterToEntity(PaymentDTO dto, @MappingTarget Payment payment) {
        payment.setMethod(PaymentMethod.fromString(dto.getMethod()));
        payment.setPaymentFor(PaymentFor.fromString(dto.getPaymentFor()));
    }
}
