package product.management.electronic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import product.management.electronic.dto.Category.*;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.CategoryService;
import java.util.List;
import java.util.UUID;
import static product.management.electronic.constants.MessageConstant.*;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all category")
    @GetMapping("/getAllCategories")
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), categories));
    }

    @Operation(summary = "Get list category by type")
    @GetMapping("/getCategoriesByType/{type}")
    public ResponseEntity<ApiResponse> getCategoriesByType(@PathVariable String type) {
        List<CategoryDto> categories = categoryService.findByType(type);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(),categories));
    }
    @GetMapping("/getCategoriesByName/{name}")
    public ResponseEntity<ApiResponse> getCategoriesByName(@PathVariable String name) {
        List<CategoryDto> categories = categoryService.findCategoryByName(name);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(),categories));
    }
    @Operation(summary = "Add new category")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody AddCategoryDto addCategoryDto) {
        categoryService.addCategory(addCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(HttpStatus.CREATED.value(), ITEM_CREATED_SUCCESS));
    }
    @Operation(summary = "Update category")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID id,  @Valid @RequestBody UpdateCategoryDto categoryDto) {
        categoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Category updated successfully"));
    }

    @Operation(summary = "Delete category by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), ITEM_DELETED_SUCCESS));
    }

}
