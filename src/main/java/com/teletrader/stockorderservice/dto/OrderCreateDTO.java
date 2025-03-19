package com.teletrader.stockorderservice.dto;

import com.teletrader.stockorderservice.model.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDTO {
    @NotNull(message = "Order type is required.")
    private OrderType orderType;

    @NotNull(message = "Price is required.")
    @Positive(message = "Price must be greater than 0.")
    private BigDecimal price;

    @NotNull(message = "Starting amount is required.")
    @Min(value = 1, message = "Starting amount must be at least 1.")
    private Integer startingAmount;
}
