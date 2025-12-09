package ir.bahman.library.mapper;

import ir.bahman.library.dto.CategoryDTO;
import ir.bahman.library.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper extends BaseMapper<Category, CategoryDTO> {
}
