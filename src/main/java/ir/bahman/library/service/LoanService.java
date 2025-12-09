package ir.bahman.library.service;

import ir.bahman.library.model.Loan;

public interface LoanService extends BaseService<Loan, Long> {
    Loan borrowBook(Long memberId, Long copyId);

    void returnBook(Long loanId);

    void checkOverdueLoans();
}
