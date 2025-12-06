package ir.bahman.library.service;

import ir.bahman.library.model.Book;

import java.util.List;

public interface BookService extends BaseService<Book, Long> {
    void assignTagToBook(String tag, Long bookId);
    List<Book> search(String keyword);
}
