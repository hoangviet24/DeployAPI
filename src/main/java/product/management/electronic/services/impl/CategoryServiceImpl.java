package product.management.electronic.services.impl;

import org.springframework.stereotype.Service;
import product.management.electronic.dto.Category.AddCategoryDto;
import product.management.electronic.dto.Category.CategoryDto;
import product.management.electronic.entities.Category;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.CategoryMapper;
import product.management.electronic.repository.CategoryRepository;
import product.management.electronic.services.CategoryService;
import product.management.electronic.dto.Category.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static product.management.electronic.constants.MessageConstant.*;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryMapper.toListDto(categoryRepository.findAll());
    }

    @Override
    public List<CategoryDto> findByType(String type) {
        if (type == null || type.trim().isEmpty() || type.matches(".*[^a-zA-Z0-9].*")) {
            throw new BadRequestException(FIELD_INVALID);
        }
        List<Category> categoryList = categoryRepository.findByType(type);
        if (categoryList.isEmpty()) {
            throw new BadRequestException(VALUE_NO_EXIST);
        }
        return categoryMapper.toListDto(categoryList);
    }

    @Override
    public Category findCategoryByName(String name) {
        return (categoryRepository.findByName(name));
    }

    @Override
    public void addCategory(AddCategoryDto addCategoryDto) {
        boolean existCategory = categoryRepository.findByName(addCategoryDto.getName()) != null;
        if (existCategory) {
            throw new BadRequestException(VALUE_EXISTED);
        }
        Category category = categoryMapper.toCreateEntity(addCategoryDto);
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(UUID id, UpdateCategoryDto updateCategoryDto) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            throw new BadRequestException(FIELD_INVALID);
        }
        Category category = optionalCategory.get();
        boolean existCategory = categoryRepository.findByName(updateCategoryDto.getName()) != null;
        if (!category.getName().equals(updateCategoryDto.getName()) && existCategory) {
            throw new BadRequestException(VALUE_EXISTED);
        }
        categoryMapper.toUpdateEntity(updateCategoryDto, category);
        categoryRepository.save(category);
    }
    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
    }

    @Override
    public void deleteCategory(UUID id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            throw new ResourceNotFoundException(CATEGORY_NOT_FOUND);
        }
        categoryRepository.delete(optionalCategory.get());
    }
}
