package com.teletrader.stockorderservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_order_type_price", columnList = "order_type, price")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    @NotNull(message = "Order type is required.")
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @NotNull(message = "Order status is required.")
    private OrderStatus orderStatus = OrderStatus.ACTIVE;

    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    @NotNull(message = "Price is required.")
    @Positive(message = "Price must be greater than 0.")
    private BigDecimal price;

    @Column(name = "starting_amount", nullable = false)
    @NotNull(message = "Starting amount is required.")
    @Min(value = 1, message = "Starting amount must be at least 1.")
    private Integer startingAmount;

    @Column(name = "remaining_amount", nullable = false)
    @NotNull(message = "Remaining amount is required.")
    @Min(value = 0, message = "Remaining amount cannot be negative.")
    private Integer remainingAmount;

    @Column(name = "creation_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Creation time is required.")
    private LocalDateTime creationTime;
}

