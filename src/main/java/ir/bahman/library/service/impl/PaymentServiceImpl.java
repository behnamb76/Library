package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.PaymentRepository;
import ir.bahman.library.Repository.PenaltyRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.exception.AccessDeniedException;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Payment;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.enums.PaymentFor;
import ir.bahman.library.model.enums.PaymentMethod;
import ir.bahman.library.model.enums.PenaltyStatus;
import ir.bahman.library.service.PaymentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentServiceImpl extends BaseServiceImpl<Payment, Long> implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PenaltyRepository penaltyRepository;
    private final PersonRepository personRepository;

    public PaymentServiceImpl(JpaRepository<Payment, Long> repository, PaymentRepository paymentRepository, PenaltyRepository penaltyRepository, PersonRepository personRepository) {
        super(repository);
        this.paymentRepository = paymentRepository;
        this.penaltyRepository = penaltyRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Payment payPenalty(Long penaltyId, PaymentMethod method, String username) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found!"));

        Person requester = personRepository.findByAccountUsername(username)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Person not found!"));

        boolean isOwner = penalty.getLoan().getMember().getId().equals(requester.getId());
        boolean isStaff = requester.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("LIBRARIAN"));

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("This penalty does not belong to you.");
        }

        if (penalty.getStatus() == PenaltyStatus.PAID) {
            throw new AlreadyExistsException("Penalty already paid");
        }

        Payment payment = Payment.builder()
                .amount(penalty.getAmount())
                .paymentDate(LocalDateTime.now())
                .method(method)
                .paymentFor(PaymentFor.PENALTY)
                .member(penalty.getLoan().getMember())
                .build();

        Payment savedPayment = persist(payment);

        penalty.setPayment(savedPayment);
        penalty.setStatus(PenaltyStatus.PAID);

        penaltyRepository.save(penalty);

        return savedPayment;
    }

    @Override
    public Payment update(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found!"));

        existing.setAmount(payment.getAmount());
        existing.setPaymentDate(payment.getPaymentDate());
        existing.setMethod(payment.getMethod());
        existing.setPaymentFor(payment.getPaymentFor());

        return paymentRepository.save(payment);
    }
}
