package com.teletrader.stockorderservice.service;

import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.mapper.OrderMapper;
import com.teletrader.stockorderservice.model.Order;
import com.teletrader.stockorderservice.model.OrderStatus;
import com.teletrader.stockorderservice.model.OrderType;
import com.teletrader.stockorderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testPlaceOrder_whenValidOrder_thenReturnsOrderDTO() {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO(OrderType.BUY, new BigDecimal("100.50"), 10);
        Order order = Order.builder()
                .id(1L)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(10)
                .creationTime(LocalDateTime.now())
                .orderStatus(OrderStatus.ACTIVE)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(10)
                .creationTime(order.getCreationTime())
                .build();

        when(orderMapper.orderCreateDTOToOrder(orderCreateDTO)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        CompletableFuture<OrderDTO> resultFuture = orderService.placeOrder(orderCreateDTO);
        OrderDTO result = resultFuture.join();

        assertEquals(orderDTO, result);
        verify(orderRepository, atLeast(1)).save(any(Order.class));
    }

    @Test
    public void testPlaceOrder_whenSellOrderIsMatched_thenUpdatesStatus() {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO(OrderType.SELL, new BigDecimal("100.50"), 10);

        Order incomingOrder = Order.builder()
                .id(1L)
                .orderType(OrderType.SELL)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(10)
                .creationTime(LocalDateTime.now())
                .orderStatus(OrderStatus.ACTIVE)
                .build();

        Order matchedOrder = Order.builder()
                .id(2L)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(0) // Fully matched
                .creationTime(LocalDateTime.now())
                .orderStatus(OrderStatus.MATCHED)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .orderType(OrderType.SELL)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(0)
                .creationTime(incomingOrder.getCreationTime())
                .build();

        when(orderMapper.orderCreateDTOToOrder(orderCreateDTO)).thenReturn(incomingOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(incomingOrder);
        when(orderRepository.findMatchingBuyOrders(any(), anyInt())).thenReturn(List.of(matchedOrder));
        when(orderMapper.orderToOrderDTO(any(Order.class))).thenReturn(orderDTO);

        CompletableFuture<OrderDTO> resultFuture = orderService.placeOrder(orderCreateDTO);
        OrderDTO result = resultFuture.join();

        assertEquals(orderDTO, result);
        verify(orderRepository, atLeast(1)).save(any(Order.class));
        verify(orderRepository, times(1)).findMatchingBuyOrders(any(), anyInt());
    }

    @Test
    public void testPlaceOrder_whenPartialMatch_thenUpdatesMatchedOrderRemainingAmount() {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO(OrderType.BUY, new BigDecimal("100.00"), 5);

        Order incomingOrder = Order.builder()
                .id(1L)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("100.00"))
                .startingAmount(5)
                .remainingAmount(5)
                .creationTime(LocalDateTime.now())
                .orderStatus(OrderStatus.ACTIVE)
                .build();

        Order matchedOrder = Order.builder()
                .id(2L)
                .orderType(OrderType.SELL)
                .price(new BigDecimal("100.00"))
                .startingAmount(10)
                .remainingAmount(10)
                .creationTime(LocalDateTime.now())
                .orderStatus(OrderStatus.ACTIVE)
                .build();

        when(orderMapper.orderCreateDTOToOrder(orderCreateDTO)).thenReturn(incomingOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(incomingOrder);
        when(orderRepository.findMatchingSellOrders(any(), anyInt())).thenReturn(List.of(matchedOrder));

        CompletableFuture<OrderDTO> resultFuture = orderService.placeOrder(orderCreateDTO);
        OrderDTO result = resultFuture.join();

        assertEquals(5, matchedOrder.getRemainingAmount());
        verify(orderRepository, times(1)).save(matchedOrder);
    }

    @Test
    public void testGetTopBuyOrders_whenOrdersExist_thenReturnsOrderDTOList() {
        List<Order> buyOrders = Arrays.asList(
                Order.builder().id(1L).orderType(OrderType.BUY).price(new BigDecimal("105.00")).startingAmount(5).remainingAmount(5).creationTime(LocalDateTime.now()).orderStatus(OrderStatus.ACTIVE).build(),
                Order.builder().id(2L).orderType(OrderType.BUY).price(new BigDecimal("103.50")).startingAmount(3).remainingAmount(3).creationTime(LocalDateTime.now()).orderStatus(OrderStatus.ACTIVE).build()
        );

        List<OrderDTO> buyOrderDTOs = buyOrders.stream()
                .map(order -> OrderDTO.builder()
                        .id(order.getId())
                        .orderType(order.getOrderType())
                        .price(order.getPrice())
                        .startingAmount(order.getStartingAmount())
                        .remainingAmount(order.getRemainingAmount())
                        .creationTime(order.getCreationTime())
                        .build())
                .collect(Collectors.toList());

        when(orderRepository.findTopBuyOrders()).thenReturn(buyOrders);
        when(orderMapper.orderToOrderDTO(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return buyOrderDTOs.stream().filter(dto -> dto.getId().equals(order.getId())).findFirst().orElse(null);
        });

        CompletableFuture<List<OrderDTO>> resultFuture = orderService.getTopBuyOrders();
        List<OrderDTO> result = resultFuture.join();

        assertEquals(buyOrderDTOs, result);
        verify(orderRepository, times(1)).findTopBuyOrders();
    }

    @Test
    public void testGetTopSellOrders_whenOrdersExist_thenReturnsOrderDTOList() {
        List<Order> sellOrders = Arrays.asList(
                Order.builder().id(3L).orderType(OrderType.SELL).price(new BigDecimal("98.00")).startingAmount(8).remainingAmount(8).creationTime(LocalDateTime.now()).orderStatus(OrderStatus.ACTIVE).build(),
                Order.builder().id(4L).orderType(OrderType.SELL).price(new BigDecimal("99.50")).startingAmount(12).remainingAmount(12).creationTime(LocalDateTime.now()).orderStatus(OrderStatus.ACTIVE).build()
        );

        List<OrderDTO> sellOrderDTOs = sellOrders.stream()
                .map(order -> OrderDTO.builder()
                        .id(order.getId())
                        .orderType(order.getOrderType())
                        .price(order.getPrice())
                        .startingAmount(order.getStartingAmount())
                        .remainingAmount(order.getRemainingAmount())
                        .creationTime(order.getCreationTime())
                        .build())
                .collect(Collectors.toList());

        when(orderRepository.findTopSellOrders()).thenReturn(sellOrders);
        when(orderMapper.orderToOrderDTO(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return sellOrderDTOs.stream().filter(dto -> dto.getId().equals(order.getId())).findFirst().orElse(null);
        });

        CompletableFuture<List<OrderDTO>> resultFuture = orderService.getTopSellOrders();
        List<OrderDTO> result = resultFuture.join();

        assertEquals(sellOrderDTOs, result);
        verify(orderRepository, times(1)).findTopSellOrders();
    }

    @Test
    public void testGetTopBuyOrders_whenNoOrders_thenReturnsEmptyList() {
        when(orderRepository.findTopBuyOrders()).thenReturn(List.of());

        CompletableFuture<List<OrderDTO>> resultFuture = orderService.getTopBuyOrders();
        List<OrderDTO> result = resultFuture.join();

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findTopBuyOrders();
    }

    @Test
    public void testGetTopSellOrders_whenNoOrders_thenReturnsEmptyList() {
        when(orderRepository.findTopSellOrders()).thenReturn(List.of());

        CompletableFuture<List<OrderDTO>> resultFuture = orderService.getTopSellOrders();
        List<OrderDTO> result = resultFuture.join();

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findTopSellOrders();
    }
}
