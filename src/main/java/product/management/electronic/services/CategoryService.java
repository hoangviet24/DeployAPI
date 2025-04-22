package product.management.electronic.services;

import product.management.electronic.dto.Category.AddCategoryDto;
import product.management.electronic.dto.Category.CategoryDto;
import product.management.electronic.dto.Category.UpdateCategoryDto;
import product.management.electronic.entities.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryDto> getAllCategories();

    List<CategoryDto> findByType (String type);

    void addCategory(AddCategoryDto addCategoryDto);

    List<CategoryDto> findCategoryByName(String name);

    void updateCategory(UUID id , UpdateCategoryDto updateCategoryDto) ;

    Category getCategoryById(UUID categoryId);

    void deleteCategory(UUID id);
}