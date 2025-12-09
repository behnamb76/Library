package ir.bahman.library.model.enums;

public enum PenaltyReason {
    OVERDUE,
    LOST,
    DAMAGED,
    OVERDUE_DAMAGED;

    public static PenaltyReason fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Penalty Reason is wrong");
        }
        for (PenaltyReason reason : PenaltyReason.values()) {
            if (reason.name().equalsIgnoreCase(text.trim())) {
                return reason;
            }
        }
        throw new IllegalArgumentException("There is no penalty reason named: " + text);
    }
}
