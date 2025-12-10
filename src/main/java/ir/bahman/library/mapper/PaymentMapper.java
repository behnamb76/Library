package ir.bahman.library.mapper;

import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.dto.PaymentDTO;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Payment;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.enums.PaymentFor;
import ir.bahman.library.model.enums.PaymentMethod;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PaymentMapper implements BaseMapper<Payment, PaymentDTO> {
    @Autowired
    private PersonRepository personRepository;

    public abstract Payment toEntity(PaymentDTO dto);

    public abstract PaymentDTO toDto(Payment payment);

    @AfterMapping
    protected void afterToDto(Payment payment, @MappingTarget PaymentDTO dto) {
        dto.setMethod(payment.getMethod().toString());
        dto.setPaymentFor(payment.getPaymentFor().toString());

        if (payment.getMember() != null) {
            dto.setMemberId(payment.getMember().getId());
        }
    }

    @AfterMapping
    protected void afterToEntity(PaymentDTO dto, @MappingTarget Payment payment) {
        payment.setMethod(PaymentMethod.fromString(dto.getMethod()));
        payment.setPaymentFor(PaymentFor.fromString(dto.getPaymentFor()));

        if (dto.getMemberId() != null) {
            Person member = personRepository.findById(dto.getMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Member not found!"));
            payment.setMember(member);
        }
    }
}
