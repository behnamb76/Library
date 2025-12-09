package ir.bahman.library.mapper;

import ir.bahman.library.Repository.CategoryRepository;
import ir.bahman.library.dto.BookDTO;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.Category;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BookMapper implements BaseMapper<Book, BookDTO> {
    @Autowired
    private CategoryRepository categoryRepository;

    public abstract Book toEntity(BookDTO dto);

    public abstract BookDTO toDto(Book book);

    @AfterMapping
    protected void afterToEntity(BookDTO dto, @MappingTarget Book book) {
        if (dto.getCategoryName() != null) {
            Category category = categoryRepository.findByNameIgnoreCase(dto.getCategoryName())
                    .orElseThrow(() -> new EntityNotFoundException("Category with name " + dto.getCategoryName() + " not found"));
            book.setCategory(category);
        }
    }

    @AfterMapping
    protected void afterToDto(Book book, @MappingTarget BookDTO dto) {
        if (book.getCategory().getName() != null) {
            Category category = categoryRepository.findByNameIgnoreCase(dto.getCategoryName())
                    .orElseThrow(() -> new EntityNotFoundException("Category with name " + dto.getCategoryName() + " not found"));
            dto.setCategoryName(category.getName());
        }
    }
}
