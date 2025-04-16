package product.management.electronic.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import product.management.electronic.dto.Product.AddProductDto;
import product.management.electronic.dto.Product.ProductDto;
import product.management.electronic.entities.Category;
import product.management.electronic.entities.Product;
import product.management.electronic.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    public ProductMapper(ModelMapper modelMapper, CategoryRepository categoryRepository) {
        this.modelMapper = modelMapper;
        this.categoryRepository = categoryRepository;
    }

    public ProductDto toDTO(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        dto.setCategoryId(product.getCategory().getId());
        return dto;
    }
    public Product toEntity(AddProductDto dto, Category category) {
        Product product = modelMapper.map(dto, Product.class);
        product.setId(null);
        product.setCategory(category);
        return product;
    }
    public List<ProductDto> toDtoList(List<Product> products) {
        return products.stream().map(product -> new ProductDto(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getQuantity(),
                product.getThumbnail(),
                product.getImages(),
                product.getCategory().getId(),
                product.isFeatured()
        )).collect(Collectors.toList());
    }
}