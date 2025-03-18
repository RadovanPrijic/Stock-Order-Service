package com.teletrader.stockorderservice.controller;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@CrossOrigin
public class OrderRestController {
    private final OrderService orderService;

    @PostMapping("/place")
    public CompletableFuture<ResponseEntity<?>> placeOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        return orderService.placeOrder(orderCreateDTO)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/top10/buy")
    public ResponseEntity<?> getTopBuyOrders() {
        return ResponseEntity.ok(orderService.getTopBuyOrders());
    }

    @GetMapping("/top10/sell")
    public ResponseEntity<?> getTopSellOrders() {
        return ResponseEntity.ok(orderService.getTopSellOrders());
    }
}
