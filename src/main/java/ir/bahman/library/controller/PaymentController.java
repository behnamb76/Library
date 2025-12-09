package ir.bahman.library.controller;

import ir.bahman.library.dto.PayPenaltyRequest;
import ir.bahman.library.dto.PaymentDTO;
import ir.bahman.library.mapper.PaymentMapper;
import ir.bahman.library.model.Payment;
import ir.bahman.library.model.enums.PaymentMethod;
import ir.bahman.library.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> payPenalty(@Valid @RequestBody PayPenaltyRequest request) {
        Payment payment = paymentService.payPenalty(request.getPenaltyId(), PaymentMethod.fromString(request.getMethod()));
        return ResponseEntity.ok().body(paymentMapper.toDto(payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePayment(@PathVariable Long id, @RequestBody PaymentDTO dto) {
        paymentService.update(id, paymentMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok().body(paymentMapper.toDto(payment));
    }
}
