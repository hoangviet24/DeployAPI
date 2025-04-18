package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import product.management.electronic.dto.Product.AddProductDto;
import org.springframework.web.server.ResponseStatusException;
import product.management.electronic.dto.Product.ProductDto;
import product.management.electronic.entities.Category;
import product.management.electronic.entities.Product;
import product.management.electronic.exceptions.ConflictException;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.ProductMapper;
import product.management.electronic.repository.ProductRepository;
import product.management.electronic.services.CategoryService;
import product.management.electronic.services.Cloudinary.CloudinaryService;
import product.management.electronic.services.ProductService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static product.management.electronic.constants.MessageConstant.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public ProductDto addProduct(AddProductDto addProductDto) {
        if (productRepository.existsBySku(addProductDto.getSku())) {
            throw new ConflictException("SKU already exists");
        }
        Category category = categoryService.getCategoryById(addProductDto.getCategoryId());
        Product product = productMapper.toEntity(addProductDto, category);
        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public List<ProductDto> getProducts(int page, int size, boolean sort, String sortBy) {
        Pageable pageable;
        if (sort) {
            Sort sorting = Sort.by(sortBy).descending();
            pageable = PageRequest.of(page, size, sorting);
        }else if(sort == false){
            Sort sorting = Sort.by(sortBy).ascending();
            pageable = PageRequest.of(page,size,sorting);
        }
        else {
            pageable = PageRequest.of(page, size);
        }
        return productMapper.toDtoList(productRepository.findAll(pageable).getContent());
    }

    @Override
    public List<ProductDto> getByName(String name, int page, int size, boolean sort, String sortBy) {
        Pageable pageable;
        if (sort) {
            Sort sorting = Sort.by(sortBy).descending();
            pageable = PageRequest.of(page, size, sorting);
        } else if(sort == false){
            Sort sorting = Sort.by(sortBy).ascending();
            pageable = PageRequest.of(page,size,sorting);
        }
        else {
            pageable = PageRequest.of(page, size);
        }
        List<Product> products = productRepository.findByNameContaining(name, pageable)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return productMapper.toDtoList(products);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(UUID productId, AddProductDto addProductDto) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND));
        if (!existingProduct.getSku().equals(addProductDto.getSku()) &&
                productRepository.existsBySku(addProductDto.getSku())) {
            throw new ConflictException(SKU_EXISTED);
        }
        Category category = categoryService.getCategoryById(addProductDto.getCategoryId());
        existingProduct.setName(addProductDto.getName());
        existingProduct.setSku(addProductDto.getSku());
        existingProduct.setDescription(addProductDto.getDescription());
        existingProduct.setBrand(addProductDto.getBrand());
        existingProduct.setPrice(addProductDto.getPrice());
        existingProduct.setQuantity(addProductDto.getQuantity());
        existingProduct.setThumbnail(addProductDto.getThumbnail());
        existingProduct.setImages(addProductDto.getImages());
        existingProduct.setCategory(category);
        existingProduct.setFeatured(addProductDto.isFeatured());
        return productMapper.toDTO(productRepository.save(existingProduct));
    }

    @Override
    public void deleteById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND + " " + id));
        productRepository.delete(product);
    }

    @Override
    public ProductDto getById(UUID uuid) {
        Product product = productRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND + " " + uuid));
        return productMapper.toDTO(product);
    }

    @Override
    public Product getProductById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
    }

    @Override
    public Map<UUID, Product> getAll(List<UUID> productIds) {
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    @Override
    @Transactional
    public ProductDto updateProductImages(UUID productId, MultipartFile thumbnail, List<MultipartFile> images) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND));
        try {
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String uniqueFileName = productId + "_thumbnail";
                Map<String, String> uploadResult = cloudinaryService.uploadImage(
                        thumbnail, "products/thumbnails", uniqueFileName);
                existingProduct.setThumbnail(uploadResult.get("url"));
            }
            if (images != null && !images.isEmpty()) {
                existingProduct.setImages(new ArrayList<>());

                List<String> imageUrls = new ArrayList<>();
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    if (image != null && !image.isEmpty()) {
                        String uniqueFileName = productId + "_image_" + i;
                        Map<String, String> uploadResult = cloudinaryService.uploadImage(
                                image, "products/images", uniqueFileName);
                        imageUrls.add(uploadResult.get("url"));
                    }
                }
                existingProduct.setImages(imageUrls);
            }
            return productMapper.toDTO(productRepository.save(existingProduct));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }
    }
    public void saveProduct(Product product) {
        productRepository.save(product);
    }
}