package product.management.electronic.mapper;

import org.springframework.stereotype.Component;
import product.management.electronic.dto.Category.AddCategoryDto;
import product.management.electronic.dto.Category.UpdateCategoryDto;
import product.management.electronic.entities.Category;
import product.management.electronic.dto.Category.CategoryDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .build();
    }

    public Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setType(dto.getType());
        return category;
    }

    public List<CategoryDto> toListDto(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Category toCreateEntity(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .type(categoryDto.getType())
                .build();
    }

    public void toUpdateEntity(UpdateCategoryDto dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.getName());
        entity.setType(dto.getType());
    }

    public Category toCreateEntity(AddCategoryDto addCategoryDto) {
        return Category.builder()
                .name(addCategoryDto.getName())
                .type(addCategoryDto.getType())
                .build();
    }
}
