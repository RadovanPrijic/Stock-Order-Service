package com.teletrader.stockorderservice.controller;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@CrossOrigin
@Tag(name = "Orders API", description = "Endpoints for order processing and storing")
public class OrderRestController {
    private final OrderService orderService;

    @PostMapping("/place")
    @Operation(summary = "Place an order", description = "Places a new order and returns the order details.")
    public CompletableFuture<ResponseEntity<OrderDTO>> placeOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        return orderService.placeOrder(orderCreateDTO)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/top10/buy")
    @Operation(summary = "Get top 10 buy orders", description = "Retrieves the top 10 buy orders.")
    public CompletableFuture<ResponseEntity<List<OrderDTO>>> getTopBuyOrders() {
        return orderService.getTopBuyOrders()
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/top10/sell")
    @Operation(summary = "Get top 10 sell orders", description = "Retrieves the top 10 sell orders.")
    public CompletableFuture<ResponseEntity<List<OrderDTO>>> getTopSellOrders() {
        return orderService.getTopSellOrders()
                .thenApply(ResponseEntity::ok);
    }
}
