package product.management.electronic.services;

import org.springframework.web.multipart.MultipartFile;
import product.management.electronic.dto.Product.AddProductDto;
import product.management.electronic.dto.Product.ProductDto;
import product.management.electronic.entities.Product;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductService {
    ProductDto addProduct(AddProductDto addProductDto);
    List<ProductDto> getProducts(int page, int size, boolean sort, String sortBy);
    List<ProductDto> getByName(String name, int page, int size, boolean sort, String sortBy);
    ProductDto updateProduct(UUID productId, AddProductDto addProductDto);
    void deleteById(UUID id);
    ProductDto getById(UUID uuid);
    Product getProductById(UUID id);
    Map<UUID, Product> getAll(List<UUID> productIds);
    ProductDto updateProductImages(UUID productId, MultipartFile thumbnail, List<MultipartFile> images);
    void saveProduct(Product product);
}