package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.CategoryRepository;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Category;
import ir.bahman.library.service.CategoryService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends BaseServiceImpl<Category, Long> implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(JpaRepository<Category, Long> repository, CategoryRepository categoryRepository) {
        super(repository);
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void prePersist(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new AlreadyExistsException("Category with this name already exists");
        }
    }

    @Override
    public Category update(Long id, Category category) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(category.getName(), id)) {
            throw new AlreadyExistsException("Category with this name already exists");
        }

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found!"));

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());

        return categoryRepository.save(category);
    }
}
