package ir.bahman.library.model.enums;

public enum PaymentMethod {
    CASH,
    CARD,
    ONLINE;

    public static PaymentMethod fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is wrong");
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(text.trim())) {
                return method;
            }
        }
        throw new IllegalArgumentException("There is no payment method named: " + text);
    }
}
