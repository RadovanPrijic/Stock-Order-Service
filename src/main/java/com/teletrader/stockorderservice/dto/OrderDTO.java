package com.teletrader.stockorderservice.dto;

import com.teletrader.stockorderservice.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private OrderType orderType;
    private BigDecimal price;
    private Integer startingAmount;
    private Integer remainingAmount;
    private LocalDateTime creationTime;
}