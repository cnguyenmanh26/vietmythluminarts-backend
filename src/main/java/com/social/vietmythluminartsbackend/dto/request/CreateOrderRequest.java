package com.social.vietmythluminartsbackend.dto.request;

import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an order (checkout)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    private PaymentMethod paymentMethod;

    private String notes;

    @Min(value = 0, message = "Discount cannot be negative")
    @Builder.Default
    private Double discount = 0.0;
}

