package com.teletrader.stockorderservice.dto;

import com.teletrader.stockorderservice.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    private OrderType orderType;
    private BigDecimal price;
    private Integer startingAmount;
}
