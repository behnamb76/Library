package ir.bahman.library.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.ISBN;

import java.time.Year;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author name must not exceed 100 characters")
    private String author;

    @NotBlank(message = "ISBN is required")
    @ISBN(type = ISBN.Type.ANY, message = "Invalid ISBN format or checksum")
    private String isbn;

    @NotBlank(message = "Publisher is required")
    @Size(max = 100, message = "Publisher name must not exceed 100 characters")
    private String publisher;

    @NotNull(message = "Publication year is required")
    @PastOrPresent(message = "Publication year cannot be in the future")
    private Year publicationYear;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Positive(message = "Edition must be a positive number")
    private Integer edition;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String categoryName;
}
