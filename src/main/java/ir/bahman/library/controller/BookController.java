package ir.bahman.library.controller;

import ir.bahman.library.dto.BookDTO;
import ir.bahman.library.mapper.BookMapper;
import ir.bahman.library.model.Book;
import ir.bahman.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
public class BookController {
    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO dto) {
        Book book = bookService.persist(bookMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toDto(book));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBook(@Valid @RequestBody BookDTO dto, @PathVariable Long id) {
        bookService.update(id, bookMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search/{keyword}")
    public  ResponseEntity<List<BookDTO>> searchBook(@PathVariable String keyword) {
        List<BookDTO> books = bookService.search(keyword)
                .stream().map(bookMapper::toDto).toList();
        return ResponseEntity.ok().body(books);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}/tag")
    public ResponseEntity<Void> assignTagToBook(@PathVariable Long id, @RequestParam String tag) {
        bookService.assignTagToBook(tag, id);
        return ResponseEntity.ok().build();
    }
}
