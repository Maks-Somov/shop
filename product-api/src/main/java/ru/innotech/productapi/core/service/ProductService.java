package ru.innotech.productapi.core.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final DiscountApi discountApi;
    private final ProductMapper productMapper;
    private final MetricsService metricsService;
    private final ProductRepository productRepository;

    @Transactional
    public void createProduct(ProductRequest productRequest) {
        try (var op = MDC.putCloseable("op", "createProduct")) {
            log.info("Create product: start");
            Product product = productMapper.toEntity(productRequest);
            productRepository.save(product);
            if (product.getId() != null) {
                try (var idc = MDC.putCloseable("productId", String.valueOf(product.getId()))) {
                    log.info("Create product: saved with id={}", product.getId());
                }
            } else {
                log.warn("Create product: saved entity has null id (check ID generation)");
            }
        } catch (Exception e) {
            metricsService.incrementErrorCounter("createProduct");
            log.error("Create product: failed - {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        try (var op = MDC.putCloseable("op", "getProduct");
             var idc = MDC.putCloseable("productId", String.valueOf(id))) {

            log.debug("Get product: fetching by id");
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Get product: product not found");
                        return new NotFoundException("Product with id %s is not found".formatted(id));
                    });
            log.info("Get product: found");
            return productMapper.toDto(product);
        } catch (Exception e) {
            metricsService.incrementErrorCounter("getProduct");
            log.error("Get product: failed - {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        try (var op = MDC.putCloseable("op", "getProducts")) {
            log.debug("Get products: fetching all");
            List<ProductResponse> result = productRepository.findAll().stream()
                    .map(productMapper::toDto)
                    .collect(Collectors.toList());
            try (var size = MDC.putCloseable("batchSize", String.valueOf(result.size()))) {
                log.info("Get products: returned {}", result.size());
            }
            return result;
        } catch (Exception e) {
            metricsService.incrementErrorCounter("getProducts");
            log.error("Get products: failed - {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        try (var op = MDC.putCloseable("op", "deleteProduct");
             var idc = MDC.putCloseable("productId", String.valueOf(id))) {

            log.info("Delete product: start");
            productRepository.deleteById(id);
            log.info("Delete product: done");
        } catch (Exception e) {
            metricsService.incrementErrorCounter("deleteProduct");
            log.error("Delete product: failed - {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        try (var op = MDC.putCloseable("op", "updateProduct");
             var idc = MDC.putCloseable("productId", String.valueOf(id))) {

            log.info("Update product: start");
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Update product: product not found");
                        return new NotFoundException("Product with id %s is not found".formatted(id));
                    });

            product.setName(productRequest.name());
            product.setDescription(productRequest.description());
            product.setCurrency(productRequest.currency());
            product.setPrice(productRequest.price());

            Product saved = productRepository.save(product);
            log.info("Update product: saved");
            return productMapper.toDto(saved);
        } catch (Exception e) {
            metricsService.incrementErrorCounter("updateProduct");
            log.error("Update product: failed - {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void updateDiscounts() {
        long t0 = System.currentTimeMillis();
        try (var op = MDC.putCloseable("op", "updateDiscounts")) {
            log.info("Update discounts: fetching from DiscountApi");
            Map<Long, BigDecimal> discounts = discountApi.getDiscounts().stream()
                    .collect(Collectors.toMap(DiscountResponse::productId, DiscountResponse::discount));

            try (var size = MDC.putCloseable("batchSize", String.valueOf(discounts.size()))) {
                log.info("Update discounts: received {} discount items", discounts.size());
            }

            List<Product> products = productRepository.findAllByIdIn(discounts.keySet().stream().toList());
            products.forEach(p -> p.setDiscount(discounts.get(p.getId())));

            productRepository.saveAll(products);

            long took = System.currentTimeMillis() - t0;
            try (var updated = MDC.putCloseable("updatedCount", String.valueOf(products.size()))) {
                log.info("Update discounts: updated {} products in {} ms", products.size(), took);
            }
        } catch (Exception e) {
            metricsService.incrementErrorCounter("updateDiscounts");
            log.error("Update discounts: failed - {}", e.getMessage());
            throw e;
        }
    }
}
