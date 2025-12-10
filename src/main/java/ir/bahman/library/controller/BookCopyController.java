package ir.bahman.library.controller;

import ir.bahman.library.dto.AssignLocationRequest;
import ir.bahman.library.dto.BookCopyDTO;
import ir.bahman.library.mapper.BookCopyMapper;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.service.BookCopyService;
import ir.bahman.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/copy")
public class BookCopyController {
    private final BookCopyService bookCopyService;
    private final BookService bookService;
    private final BookCopyMapper bookCopyMapper;

    public BookCopyController(BookCopyService bookCopyService, BookService bookService, BookCopyMapper bookCopyMapper) {
        this.bookCopyService = bookCopyService;
        this.bookService = bookService;
        this.bookCopyMapper = bookCopyMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PostMapping
    public ResponseEntity<BookCopyDTO> createBookCopy(@RequestParam Long bookId) {
        Book book = bookService.findById(bookId);

        BookCopy bookCopy = bookCopyService.persist(BookCopy.builder()
                .book(book).build());

        return ResponseEntity.status(HttpStatus.CREATED).body(bookCopyMapper.toDto(bookCopy));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/assign-location")
    public ResponseEntity<Void> assignLocationToBookCopy(@RequestBody AssignLocationRequest request) {
        bookCopyService.assignLocation(request.getBookCopyId(), request.getLocationId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/inspect/{id}")
    public ResponseEntity<Void> inspectReturnedBookCopy(@PathVariable Long id, @RequestParam boolean damaged) {
        bookCopyService.inspectReturnedBookCopy(id, damaged);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/mark-lost/{id}")
    public ResponseEntity<Void> markBookCopyAsLost(@PathVariable Long id) {
        bookCopyService.markBookCopyAsLost(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<BookCopyDTO> updateBookCopy(@PathVariable Long id, @RequestBody BookCopyDTO dto) {
        BookCopy bookCopy = bookCopyService.update(id, bookCopyMapper.toEntity(dto));
        return ResponseEntity.ok().body(bookCopyMapper.toDto(bookCopy));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping("/get-pending-check")
    public ResponseEntity<List<BookCopyDTO>> getReturnedPendingCheckCopies() {
        List<BookCopyDTO> dtoList = bookCopyService.findReturnedPendingCheckCopies()
                .stream().map(bookCopyMapper::toDto).toList();

        return ResponseEntity.ok().body(dtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<BookCopyDTO> getBookCopy(@PathVariable Long id) {
        BookCopy bookCopy = bookCopyService.findById(id);
        return ResponseEntity.ok().body(bookCopyMapper.toDto(bookCopy));
    }

}
