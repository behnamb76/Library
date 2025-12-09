package ir.bahman.library.model.enums;

public enum PaymentFor {
    PENALTY,
    MEMBERSHIP;

    public static PaymentFor fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Reason is wrong");
        }
        for (PaymentFor paymentFor : PaymentFor.values()) {
            if (paymentFor.name().equalsIgnoreCase(text.trim())) {
                return paymentFor;
            }
        }
        throw new IllegalArgumentException("There is no payment reason named: " + text);
    }
}
