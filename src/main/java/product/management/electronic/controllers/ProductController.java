package product.management.electronic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import product.management.electronic.dto.Product.AddProductDto;
import product.management.electronic.dto.Product.ProductDto;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.ProductService;

import java.util.List;
import java.util.UUID;

import static product.management.electronic.constants.MessageConstant.ITEM_CREATED_SUCCESS;
import static product.management.electronic.constants.MessageConstant.ITEM_DELETED_SUCCESS;


@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Add new product")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductDto addProductDto) {
        productService.addProduct(addProductDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(HttpStatus.CREATED.value(), ITEM_CREATED_SUCCESS));
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean sort,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        int pageIndex = Math.max(0, page - 1);
        if (name != null) {
            List<ProductDto> products = productService.getByName(name, pageIndex, size, sort, sortBy);
            return ResponseEntity.ok(products);
        } else {
            List<ProductDto> products = productService.getProducts(pageIndex, size, sort, sortBy);
            return ResponseEntity.ok(products);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        productService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(HttpStatus.OK.value(), ITEM_DELETED_SUCCESS));
    }

    @Operation(summary = "Update existing product")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable UUID productId,
            @RequestBody AddProductDto addProductDto) {
        ProductDto updatedProduct = productService.updateProduct(productId, addProductDto);
        return ResponseEntity.ok()
                .body(new ApiResponse(HttpStatus.OK.value(), "Product updated successfully"));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse> getProduct(@PathVariable UUID id) {
        ProductDto productDto = productService.getById(id);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), productDto));
    }

    @Operation(summary = "Upload product images")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(value = "/{productId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProductImages(
            @PathVariable UUID productId,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        ProductDto updatedProduct = productService.updateProductImages(productId, thumbnail, images);

        return ResponseEntity.ok()
                .body(new ApiResponse(HttpStatus.OK.value(), "Product images updated successfully"));
    }
}