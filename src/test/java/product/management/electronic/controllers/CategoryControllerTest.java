package product.management.electronic.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import product.management.electronic.dto.Category.AddCategoryDto;
import product.management.electronic.dto.Category.CategoryDto;
import product.management.electronic.dto.Category.UpdateCategoryDto;
import product.management.electronic.exceptions.GlobalExceptionHandler;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.services.CategoryService;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID CATEGORY_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID CATEGORY_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID CATEGORY_ID_UPDATE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID CATEGORY_ID_DELETE = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testAddCategory_Success() throws Exception {
        AddCategoryDto addCategoryDto = new AddCategoryDto();
        addCategoryDto.setName("New Category");
        addCategoryDto.setType("Product");
        doNothing().when(categoryService).addCategory(any(AddCategoryDto.class));
        mockMvc.perform(post("/api/categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CREATED.value()));
        verify(categoryService, times(1)).addCategory(any(AddCategoryDto.class));
    }

    @Test
    void testAddCategory_InvalidFormat() throws Exception {
        String invalidJson = "{}";
        AddCategoryDto emptyDto = new AddCategoryDto();
        doThrow(new IllegalArgumentException("Invalid category data"))
                .when(categoryService).addCategory(any(AddCategoryDto.class));
        mockMvc.perform(post("/api/categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testAddCategory_DuplicateName() throws Exception {
        AddCategoryDto addCategoryDto = new AddCategoryDto();
        addCategoryDto.setName("Existing Category");
        addCategoryDto.setType("Product");
        doThrow(new IllegalArgumentException("Category with this name already exists"))
                .when(categoryService).addCategory(any(AddCategoryDto.class));
        mockMvc.perform(post("/api/categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCategoryDto)))
                .andExpect(status().isBadRequest());
        verify(categoryService, times(1)).addCategory(any(AddCategoryDto.class));
    }

    @Test
    void testGetCategoriesByType_Success() throws Exception {
        String type = "Product";
        CategoryDto category1 = new CategoryDto();
        category1.setId(CATEGORY_ID_1);
        category1.setName("Electronics");
        category1.setType(type);
        CategoryDto category2 = new CategoryDto();
        category2.setId(CATEGORY_ID_2);
        category2.setName("Clothing");
        category2.setType(type);
        List<CategoryDto> categories = Arrays.asList(category1, category2);
        when(categoryService.findByType(type)).thenReturn(categories);
        mockMvc.perform(get("/api/categories/getCategoriesByType/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"))
                .andExpect(jsonPath("$.data[1].name").value("Clothing"));
        verify(categoryService, times(1)).findByType(type);
    }

    @Test
    void testGetCategoriesByType_EmptyList() throws Exception {
        String type = "NonExistingType";
        List<CategoryDto> emptyCategories = new ArrayList<>();
        when(categoryService.findByType(type)).thenReturn(emptyCategories);
        mockMvc.perform(get("/api/categories/getCategoriesByType/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        verify(categoryService, times(1)).findByType(type);
    }

    @Test
    void testDeleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory(CATEGORY_ID_DELETE);
        mockMvc.perform(delete("/api/categories/deleteCategory/{id}", CATEGORY_ID_DELETE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()));
        verify(categoryService, times(1)).deleteCategory(CATEGORY_ID_DELETE);
    }

    @Test
    void testDeleteCategory_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found with id: " + CATEGORY_ID_DELETE))
                .when(categoryService).deleteCategory(CATEGORY_ID_DELETE);
        mockMvc.perform(delete("/api/categories/deleteCategory/{id}", CATEGORY_ID_DELETE))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(ResourceNotFoundException.class,
                        result.getResolvedException()));
        verify(categoryService, times(1)).deleteCategory(CATEGORY_ID_DELETE);
    }
}