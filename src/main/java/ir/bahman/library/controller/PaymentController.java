package ir.bahman.library.controller;

import ir.bahman.library.dto.PayPenaltyRequest;
import ir.bahman.library.dto.PaymentDTO;
import ir.bahman.library.mapper.PaymentMapper;
import ir.bahman.library.model.Payment;
import ir.bahman.library.model.enums.PaymentMethod;
import ir.bahman.library.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @PostMapping
    public ResponseEntity<PaymentDTO> payPenalty(@Valid @RequestBody PayPenaltyRequest request, Principal principal) {
        Payment payment = paymentService.payPenalty(request.getPenaltyId(), PaymentMethod.fromString(request.getMethod()), principal.getName());
        return ResponseEntity.ok().body(paymentMapper.toDto(payment));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePayment(@PathVariable Long id, @RequestBody PaymentDTO dto) {
        paymentService.update(id, paymentMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok().body(paymentMapper.toDto(payment));
    }
}
