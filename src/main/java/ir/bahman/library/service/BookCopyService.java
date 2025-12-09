package ir.bahman.library.service;

import ir.bahman.library.model.BookCopy;

import java.util.List;

public interface BookCopyService extends BaseService<BookCopy, Long> {
    List<BookCopy> findReturnedPendingCheckCopies();

    void inspectReturnedBookCopy(Long id, boolean damaged);

    void markBookCopyAsLost (Long id);

    void assignLocation(Long bookCopyId, Long locationId);
}
