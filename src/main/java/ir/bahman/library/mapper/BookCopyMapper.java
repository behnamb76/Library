package ir.bahman.library.mapper;

import ir.bahman.library.Repository.BookRepository;
import ir.bahman.library.Repository.LocationRepository;
import ir.bahman.library.dto.BookCopyDTO;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Location;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BookCopyMapper implements BaseMapper<BookCopy, BookCopyDTO> {
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private BookRepository bookRepository;

    public abstract BookCopy toEntity(BookCopyDTO dto);

    public abstract BookCopyDTO toDto(BookCopy bookCopy);

    @AfterMapping
    protected void afterToDto(BookCopy bookCopy, @MappingTarget BookCopyDTO dto) {
        dto.setBookId(bookCopy.getBook().getId());
        if (bookCopy.getLocation() != null){
            dto.setLocationId(bookCopy.getLocation().getId());
        }
    }

    @AfterMapping
    protected void afterToEntity(BookCopyDTO dto, @MappingTarget BookCopy bookCopy) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found!"));
        bookCopy.setBook(book);

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found!"));
        bookCopy.setLocation(location);
    }
}
