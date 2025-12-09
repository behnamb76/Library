package ir.bahman.library.controller;

import ir.bahman.library.dto.CategoryDTO;
import ir.bahman.library.mapper.CategoryMapper;
import ir.bahman.library.model.Category;
import ir.bahman.library.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Category category = categoryService.persist(categoryMapper.toEntity(dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toDto(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(@Valid @RequestBody CategoryDTO dto, @PathVariable Long id) {
        categoryService.update(id, categoryMapper.toEntity(dto));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id) {
        Category category = categoryService.findById(id);

        return ResponseEntity.ok().body(categoryMapper.toDto(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> dtoList = categoryService.findAll()
                .stream().map(categoryMapper::toDto).toList();

        return ResponseEntity.ok().body(dtoList);
    }
}
