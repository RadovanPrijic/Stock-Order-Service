package com.teletrader.stockorderservice.service;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.mapper.OrderMapper;
import com.teletrader.stockorderservice.model.Order;
import com.teletrader.stockorderservice.model.OrderStatus;
import com.teletrader.stockorderservice.model.OrderType;
import com.teletrader.stockorderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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

    @Async
    @Transactional
    public CompletableFuture<OrderDTO> placeOrder(OrderCreateDTO orderCreateDTO) {
        Order order = orderMapper.orderCreateDTOToOrder(orderCreateDTO);

        order.setRemainingAmount(order.getStartingAmount());
        order.setCreationTime(LocalDateTime.now());
        order = orderRepository.save(order); // WHY NOT THIS IN matchOrder? ASK AGAIN

        return CompletableFuture.completedFuture(matchOrder(order));
    }

    private OrderDTO matchOrder(Order order) {
        List<Order> matchingOrders = order.getOrderType() == OrderType.BUY
                ? orderRepository.findMatchingSellOrders(order.getPrice(), order.getStartingAmount())
                : orderRepository.findMatchingBuyOrders(order.getPrice(), order.getStartingAmount());

        int remainingAmount = order.getStartingAmount();
        for (Order matchedOrder : matchingOrders)
            remainingAmount = executePartialTrade(matchedOrder, remainingAmount);

        if (remainingAmount > 0) {
            order.setRemainingAmount(remainingAmount);
        } else {
            order.setRemainingAmount(0);
            order.setOrderStatus(OrderStatus.MATCHED);
        }
        order = orderRepository.save(order);

        return orderMapper.orderToOrderDTO(order);
    }

    private int executePartialTrade(Order matchedOrder, int incomingOrderRemainingAmount) {
        int matchedOrderRemainingAmount = matchedOrder.getRemainingAmount();
        int matchAmount = Math.min(matchedOrderRemainingAmount, incomingOrderRemainingAmount);

        if (matchedOrderRemainingAmount > incomingOrderRemainingAmount) {
            matchedOrder.setRemainingAmount(matchedOrderRemainingAmount - incomingOrderRemainingAmount);
        } else {
            matchedOrder.setRemainingAmount(0);
            matchedOrder.setOrderStatus(OrderStatus.MATCHED);
        }
        orderRepository.save(matchedOrder);

        return incomingOrderRemainingAmount - matchAmount;
    }

    public List<OrderDTO> getTopBuyOrders() {
        return orderRepository.findTopBuyOrders()
                .stream()
                .map(orderMapper::orderToOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getTopSellOrders() {
        return orderRepository.findTopSellOrders()
                .stream()
                .map(orderMapper::orderToOrderDTO)
                .collect(Collectors.toList());
    }
}
