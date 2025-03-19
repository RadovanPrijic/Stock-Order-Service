package com.teletrader.stockorderservice.controller;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@AllArgsConstructor
public class OrderWebSocketController {
    private final OrderService orderService;

    @MessageMapping("/orders/place")
    @SendTo("/topic/order-placed")
    public CompletableFuture<OrderDTO> placeOrder(@Payload OrderCreateDTO orderCreateDTO) {
        return orderService.placeOrder(orderCreateDTO);
    }

    @MessageMapping("/orders/top10/buy")
    @SendTo("/topic/top10-buy-orders")
    public CompletableFuture<List<OrderDTO>> getTopBuyOrders() {
        return orderService.getTopBuyOrders();
    }

    @MessageMapping("/orders/top10/sell")
    @SendTo("/topic/top10-sell-orders")
    public CompletableFuture<List<OrderDTO>> getTopSellOrders() {
        return orderService.getTopSellOrders();
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception exception) {
        return "Error processing order: " + exception.getMessage();
    }
}

