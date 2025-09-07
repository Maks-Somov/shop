package ru.innotech.productapi.adapters.discount;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.innotech.productapi.adapters.discount.dto.DiscountResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountApi {
    private final DiscountClient discountClient;

    public List<DiscountResponse> getDiscounts() {
        try {
            return discountClient.getDiscounts();
        } catch (Exception ex) {
            log.error("Discount service throws exception {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
