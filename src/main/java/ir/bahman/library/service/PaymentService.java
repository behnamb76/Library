package ir.bahman.library.service;

import ir.bahman.library.model.Payment;
import ir.bahman.library.model.enums.PaymentMethod;

public interface PaymentService extends BaseService<Payment, Long> {
    Payment payPenalty(Long penaltyId, PaymentMethod method);
}
