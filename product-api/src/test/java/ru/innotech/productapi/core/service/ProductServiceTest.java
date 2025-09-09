package ru.innotech.productapi.core.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.innotech.productapi.ProductTestUtil;
import ru.innotech.productapi.adapters.controller.dto.request.ProductRequest;
import ru.innotech.productapi.adapters.controller.dto.response.ProductResponse;
import ru.innotech.productapi.adapters.discount.DiscountApi;
import ru.innotech.productapi.adapters.discount.dto.DiscountResponse;
import ru.innotech.productapi.adapters.repository.ProductRepository;
import ru.innotech.productapi.core.exception.NotFoundException;
import ru.innotech.productapi.core.mapper.ProductMapper;
import ru.innotech.productapi.core.model.Product;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private DiscountApi discountApi;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void whenGetProductByIdThenEntityNotFound() {
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> productService.getProduct(1L));
    }

    @Test
    void whenGetProductByIdThenSuccess() {
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(ProductTestUtil.product1Mock()));
        Mockito.when(productMapper.toDto(ProductTestUtil.product1Mock())).thenReturn(ProductTestUtil.product1ResponseMock(null));
        ProductResponse expected = ProductTestUtil.product1ResponseMock(null);
        ProductResponse actual = productService.getProduct(1L);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void whenDeleteProductByIdThenSuccess() {
        productService.deleteProduct(1L);
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    void whenGetAllProductsThenSuccess() {
        Mockito.when(productRepository.findAll()).thenReturn(List.of(ProductTestUtil.product1Mock(), ProductTestUtil.product2Mock()));
        Mockito.when(productMapper.toDto(ProductTestUtil.product1Mock())).thenReturn(ProductTestUtil.product1ResponseMock(null));
        Mockito.when(productMapper.toDto(ProductTestUtil.product2Mock())).thenReturn(ProductTestUtil.product2ResponseMock(null));
        List<ProductResponse> expected = List.of(ProductTestUtil.product1ResponseMock(null), ProductTestUtil.product2ResponseMock(null));
        List<ProductResponse> actual = productService.getProducts();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void whenCreateProductThenSuccess() {
        ProductRequest productRequest = ProductTestUtil.productCreateRequest();
        Mockito.when(productMapper.toEntity(productRequest)).thenReturn(ProductTestUtil.product1Mock());
        productService.createProduct(productRequest);
        Mockito.verify(productRepository, Mockito.times(1)).save(ProductTestUtil.product1Mock());
    }

    @Test
    void whenUpdateProductThenSuccess() {
        ProductResponse expected = new ProductResponse(1L, "Updated product",
                "Updated product description", BigDecimal.valueOf(87), "RUB", BigDecimal.ONE);
        ProductRequest productRequest = ProductTestUtil.productUpdateRequest();
        Product product = ProductTestUtil.product1Mock();
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setCurrency(productRequest.currency());
        product.setPrice(productRequest.price());

        Mockito.when(productMapper.toDto(product)).thenReturn(expected);
        Mockito.when(productRepository.save(product)).thenReturn(product);
        ProductResponse actual = productService.updateProduct(1L, productRequest);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void whenUpdateProductDiscountsThenSuccess() {
        List<DiscountResponse> discountResponses = List.of(new DiscountResponse(1L, BigDecimal.valueOf(0.1)), new DiscountResponse(2L, BigDecimal.valueOf(0.3)));
        Mockito.when(discountApi.getDiscounts()).thenReturn(discountResponses);
        Product product1 = ProductTestUtil.product1Mock();
        product1.setId(1L);
        Product product2 = ProductTestUtil.product2Mock();
        product2.setId(2L);
        Mockito.when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

        productService.updateDiscounts();
        Mockito.verify(productRepository, Mockito.times(1)).saveAll(List.of(product1, product2));
        Assertions.assertEquals(product1.getDiscount().stripTrailingZeros(), BigDecimal.valueOf(0.1));
        Assertions.assertEquals(product2.getDiscount().stripTrailingZeros(), BigDecimal.valueOf(0.3));
    }
}
