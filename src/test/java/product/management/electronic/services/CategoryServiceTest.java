package product.management.electronic.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.management.electronic.dto.Category.AddCategoryDto;
import product.management.electronic.dto.Category.UpdateCategoryDto;
import product.management.electronic.entities.Category;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.mapper.CategoryMapper;
import product.management.electronic.repository.CategoryRepository;
import product.management.electronic.services.impl.CategoryServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static product.management.electronic.constants.MessageConstant.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;
    private UUID categoryId;
    private Category existingCategory;
    private UpdateCategoryDto updateCategoryDto;

    @BeforeEach
    public void setUp() {
        categoryId = UUID.randomUUID();

        existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old Name");
        existingCategory.setType("Old Type");

        updateCategoryDto = new UpdateCategoryDto();
        updateCategoryDto.setName("New Name");
        updateCategoryDto.setType("New Type");
    }
    //Add
    @Test
    public void testAddCategory_Success() {
        AddCategoryDto addCategoryDto = new AddCategoryDto();
        addCategoryDto.setName("Name Category");
        addCategoryDto.setType("Type Category");

        Category category = new Category();
        category.setName(addCategoryDto.getName());
        category.setType(addCategoryDto.getType());

        when(categoryRepository.findByName(addCategoryDto.getName())).thenReturn(null);
        when(categoryMapper.toCreateEntity(addCategoryDto)).thenReturn(category);

        categoryService.addCategory(addCategoryDto);
        verify(categoryRepository).save(category);
    }

    @Test
    public void testAddCategory_Fail_Duplicate_Name() {
        AddCategoryDto addCategoryDto = new AddCategoryDto();
        addCategoryDto.setName("Name Category");
        addCategoryDto.setType("Type Category");

        Category category = new Category();
        category.setName(addCategoryDto.getName());
        category.setType(addCategoryDto.getType());

        when(categoryRepository.findByName(addCategoryDto.getName())).thenReturn(category);
        assertThrows(BadRequestException.class, () -> categoryService.addCategory(addCategoryDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }
    //Update
    @Test
    public void testUpdateCategory_Success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName("New Name")).thenReturn(null); // Không trùng tên

        categoryService.updateCategory(categoryId, updateCategoryDto);

        verify(categoryMapper).toUpdateEntity(updateCategoryDto, existingCategory);
        verify(categoryRepository).save(existingCategory);
    }
    @Test
    public void testUpdateCategory_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryId, updateCategoryDto);
        });

        assertEquals(FIELD_INVALID, ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void testUpdateCategory_NameAlreadyExists_ThrowsException() {
        Category anotherCategory = new Category();
        anotherCategory.setId(UUID.randomUUID());
        anotherCategory.setName("New Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName("New Name")).thenReturn(anotherCategory); // Đã tồn tại tên

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryId, updateCategoryDto);
        });

        assertEquals(VALUE_EXISTED, ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }
}
