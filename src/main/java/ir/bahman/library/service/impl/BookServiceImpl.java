package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.BookRepository;
import ir.bahman.library.Repository.CategoryRepository;
import ir.bahman.library.Repository.TagRepository;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.Category;
import ir.bahman.library.model.Tag;
import ir.bahman.library.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl extends BaseServiceImpl<Book, Long> implements BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public BookServiceImpl(JpaRepository<Book, Long> repository, BookRepository bookRepository, CategoryRepository categoryRepository, TagRepository tagRepository) {
        super(repository);
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    protected void prePersist(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new AlreadyExistsException("This book already exists!");
        }

        Category category = categoryRepository.findByName(book.getCategory().getName())
                .orElseThrow(() -> new EntityNotFoundException("Category not found!"));
        book.setCategory(category);
        bookRepository.save(book);
    }

    @Override
    public Book update(Long id, Book book) {
        Book existing = findById(id);

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setIsbn(book.getIsbn());
        existing.setPublisher(book.getPublisher());
        existing.setPublicationYear(book.getPublicationYear());
        existing.setDescription(book.getDescription());
        existing.setEdition(book.getEdition());
        existing.setTags(book.getTags());

        return bookRepository.save(existing);
    }

    @Override
    public void assignTagToBook(String tag, Long bookId) {
        Tag founded = tagRepository.findByName(tag.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found!"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found!"));
        book.getTags().add(founded);
        bookRepository.save(book);
    }

    @Override
    public List<Book> search(String keyword) {
        return bookRepository.searchByKeyword(keyword);
    }
}
