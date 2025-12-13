package ir.bahman.library.controller;

import ir.bahman.library.dto.BorrowBookRequest;
import ir.bahman.library.dto.LoanResponseDTO;
import ir.bahman.library.dto.LoanUpdateRequest;
import ir.bahman.library.dto.ReturnBookRequest;
import ir.bahman.library.mapper.LoanMapper;
import ir.bahman.library.mapper.LoanUpdateMapper;
import ir.bahman.library.model.Loan;
import ir.bahman.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/loan")
public class LoanController {
    private final LoanService loanService;
    private final LoanMapper loanMapper;
    private final LoanUpdateMapper loanUpdateMapper;

    public LoanController(LoanService loanService, LoanMapper loanMapper, LoanUpdateMapper loanUpdateMapper) {
        this.loanService = loanService;
        this.loanMapper = loanMapper;
        this.loanUpdateMapper = loanUpdateMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @PostMapping("/borrow-book")
    public ResponseEntity<LoanResponseDTO> borrowBook(@Valid @RequestBody BorrowBookRequest request) {
        Loan loan = loanService.borrowBook(request.getMemberId(), request.getBookCopyId());
        return ResponseEntity.ok().body(loanMapper.toDto(loan));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLoan(@PathVariable Long id, @Valid @RequestBody LoanUpdateRequest loanUpdateRequest) {
        loanService.update(id, loanUpdateMapper.toEntity(loanUpdateRequest));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/return-book")
    public ResponseEntity<Void> returnBook(@Valid @RequestBody ReturnBookRequest request) {
        loanService.returnBook(request.getLoanId(), request.getMemberId(), request.getBookCopyId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/check-loans")
    public ResponseEntity<Void> checkOverdueLoans() {
        loanService.checkOverdueLoans();
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getLoan(@PathVariable Long id) {
        Loan loan = loanService.findById(id);
        return ResponseEntity.ok().body(loanMapper.toDto(loan));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        List<LoanResponseDTO> dtoList = loanService.findAll()
                .stream().map(loanMapper::toDto).toList();

        return ResponseEntity.ok().body(dtoList);
    }
}
