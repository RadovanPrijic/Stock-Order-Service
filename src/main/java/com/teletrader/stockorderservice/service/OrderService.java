package com.teletrader.stockorderservice.service;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.mapper.OrderMapper;
import com.teletrader.stockorderservice.model.Order;
import com.teletrader.stockorderservice.model.OrderStatus;
import com.teletrader.stockorderservice.model.OrderType;
import com.teletrader.stockorderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public CompletableFuture<OrderDTO> placeOrder(OrderCreateDTO orderCreateDTO) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = orderMapper.orderCreateDTOToOrder(orderCreateDTO);

            // Persists the incoming order to the database
            order.setRemainingAmount(order.getStartingAmount());
            order.setCreationTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.ACTIVE);
            order = orderRepository.save(order);

            // Tries to match the incoming order with active orders
            Order orderMatchingResult = matchOrder(order);

            return orderMapper.orderToOrderDTO(orderMatchingResult);
        });
    }

    private Order matchOrder(Order order) {
        // Finds just enough matching sell or buy orders (if there are any) by using price-time matching
        List<Order> matchingOrders = order.getOrderType() == OrderType.BUY
                ? orderRepository.findMatchingSellOrders(order.getPrice(), order.getStartingAmount())
                : orderRepository.findMatchingBuyOrders(order.getPrice(), order.getStartingAmount());

        // Executes partial trades with the matching orders
        int remainingAmount = order.getStartingAmount();
        for (Order matchedOrder : matchingOrders)
            remainingAmount = executePartialTrade(matchedOrder, remainingAmount);

        // Determines if the incoming order has been fully matched or not (if it has, the order status is accordingly updated)
        if (remainingAmount > 0) {
            order.setRemainingAmount(remainingAmount);
        } else {
            order.setRemainingAmount(0);
            order.setOrderStatus(OrderStatus.MATCHED);
        }

        // Saves the processed order to the database
        return orderRepository.save(order);
    }

    private int executePartialTrade(Order matchedOrder, int incomingOrderRemainingAmount) {
        int matchedOrderRemainingAmount = matchedOrder.getRemainingAmount();
        int matchAmount = Math.min(matchedOrderRemainingAmount, incomingOrderRemainingAmount);

        // Determines which (incoming or matched) order is the one that has been fully matched
        if (matchedOrderRemainingAmount > incomingOrderRemainingAmount) {
            matchedOrder.setRemainingAmount(matchedOrderRemainingAmount - incomingOrderRemainingAmount);
        } else {
            matchedOrder.setRemainingAmount(0);
            matchedOrder.setOrderStatus(OrderStatus.MATCHED);
        }

        // Updates the matched order (its amount and, potentially, status have changed)
        orderRepository.save(matchedOrder);

        // Returns the remaining amount of the incoming order's stock, so it can continue being processed
        return incomingOrderRemainingAmount - matchAmount;
    }

    public CompletableFuture<List<OrderDTO>> getTopBuyOrders() {
        return CompletableFuture.supplyAsync(() ->
                orderRepository.findTopBuyOrders()
                        .stream()
                        .map(orderMapper::orderToOrderDTO)
                        .collect(Collectors.toList())
        );
    }

    public CompletableFuture<List<OrderDTO>> getTopSellOrders() {
        return CompletableFuture.supplyAsync(() ->
                orderRepository.findTopSellOrders()
                        .stream()
                        .map(orderMapper::orderToOrderDTO)
                        .collect(Collectors.toList())
        );
    }
}
