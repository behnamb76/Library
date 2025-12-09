package ir.bahman.library.model.enums;

public enum PenaltyStatus {
    UNPAID,
    PAID,
    CANCELLED;

    public static PenaltyStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Penalty status is wrong");
        }
        for (PenaltyStatus status : PenaltyStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("There is no penalty status named: " + text);
    }
}
