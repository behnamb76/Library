package ir.bahman.library.service;

import ir.bahman.library.model.Reservation;

public interface ReservationService extends BaseService<Reservation, Long> {
    Reservation reserveBook(Long bookId, Long memberId);

    void cancelReservation(Long id);

    void reorderQueue(Long bookId);

    void expireReadyForPickupReservation();
}
