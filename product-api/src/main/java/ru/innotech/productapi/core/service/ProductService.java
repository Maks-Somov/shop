package ru.innotech.productapi.core.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.productapi.adapters.controller.dto.request.ProductRequest;
import ru.innotech.productapi.adapters.controller.dto.response.ProductResponse;
import ru.innotech.productapi.adapters.discount.DiscountApi;
import ru.innotech.productapi.adapters.discount.dto.DiscountResponse;
import ru.innotech.productapi.adapters.repository.ProductRepository;
import ru.innotech.productapi.core.exception.NotFoundException;
import ru.innotech.productapi.core.mapper.ProductMapper;
import ru.innotech.productapi.core.model.Product;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final DiscountApi discountApi;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @Transactional
    public void createProduct(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s is not found", id)));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s is not found", id)));

        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setCurrency(productRequest.currency());
        product.setPrice(productRequest.price());

        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void updateDiscounts() {
        Map<Long, BigDecimal> discounts = discountApi.getDiscounts().stream()
                .collect(Collectors.toMap(DiscountResponse::productId, DiscountResponse::discount));
        List<Product> products = productRepository.findAllByIdIn(discounts.keySet().stream().toList());

        products.forEach(p -> p.setDiscount(discounts.get(p.getId())));

        productRepository.saveAll(products);
    }
}
