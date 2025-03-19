package com.teletrader.stockorderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teletrader.stockorderservice.dto.OrderCreateDTO;
import com.teletrader.stockorderservice.dto.OrderDTO;
import com.teletrader.stockorderservice.model.OrderType;
import com.teletrader.stockorderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderRestController orderRestController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testPlaceOrder_whenValidRequest_thenReturnsOrderDTO() throws Exception {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO(OrderType.BUY, new BigDecimal("100.50"), 10);

        OrderDTO expectedResponse = OrderDTO.builder()
                .id(1L)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("100.50"))
                .startingAmount(10)
                .remainingAmount(10)
                .creationTime(LocalDateTime.now())
                .build();

        when(orderService.placeOrder(any(OrderCreateDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        MvcResult result = mockMvc.perform(asyncDispatch(
                        mockMvc.perform(post("/api/orders/place")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(orderCreateDTO)))
                                .andReturn()))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        OrderDTO responseDTO = objectMapper.readValue(responseContent, OrderDTO.class);

        assertEquals(expectedResponse, responseDTO);
        verify(orderService, times(1)).placeOrder(any(OrderCreateDTO.class));
    }

    @Test
    public void testPlaceOrder_whenOrderTypeIsNull_thenReturnsBadRequest() throws Exception {
        OrderCreateDTO invalidOrder = new OrderCreateDTO(null, new BigDecimal("100.00"), 5);

        mockMvc.perform(post("/api/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderCreateDTO.class));
    }

    @Test
    public void testPlaceOrder_whenPriceIsNegative_thenReturnsBadRequest() throws Exception {
        OrderCreateDTO invalidOrder = new OrderCreateDTO(OrderType.BUY, new BigDecimal("-10.0"), 5);

        mockMvc.perform(post("/api/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderCreateDTO.class));
    }

    @Test
    public void testPlaceOrder_whenAmountIsZero_thenReturnsBadRequest() throws Exception {
        OrderCreateDTO invalidOrder = new OrderCreateDTO(OrderType.BUY, new BigDecimal("100.00"), 0);

        mockMvc.perform(post("/api/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderCreateDTO.class));
    }

    @Test
    public void testGetTopBuyOrders_whenOrdersExist_thenReturnsList() throws Exception {
        List<OrderDTO> topBuyOrders = Arrays.asList(
                OrderDTO.builder().id(1L).orderType(OrderType.BUY).price(new BigDecimal("105.00")).startingAmount(5).remainingAmount(5).creationTime(LocalDateTime.now()).build(),
                OrderDTO.builder().id(2L).orderType(OrderType.BUY).price(new BigDecimal("103.50")).startingAmount(3).remainingAmount(3).creationTime(LocalDateTime.now()).build()
        );

        when(orderService.getTopBuyOrders()).thenReturn(CompletableFuture.completedFuture(topBuyOrders));

        MvcResult result = mockMvc.perform(asyncDispatch(
                        mockMvc.perform(get("/api/orders/top10/buy"))
                                .andExpect(status().isOk())
                                .andReturn()))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<OrderDTO> responseDTOs = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class));

        assertEquals(topBuyOrders, responseDTOs);
        verify(orderService, times(1)).getTopBuyOrders();
    }

    @Test
    public void testGetTopSellOrders_whenOrdersExist_thenReturnsList() throws Exception {
        List<OrderDTO> topSellOrders = Arrays.asList(
                OrderDTO.builder().id(3L).orderType(OrderType.SELL).price(new BigDecimal("98.00")).startingAmount(8).remainingAmount(8).creationTime(LocalDateTime.now()).build(),
                OrderDTO.builder().id(4L).orderType(OrderType.SELL).price(new BigDecimal("99.50")).startingAmount(12).remainingAmount(12).creationTime(LocalDateTime.now()).build()
        );

        when(orderService.getTopSellOrders()).thenReturn(CompletableFuture.completedFuture(topSellOrders));

        MvcResult result = mockMvc.perform(asyncDispatch(
                    mockMvc.perform(get("/api/orders/top10/sell"))
                            .andExpect(status().isOk())
                            .andReturn()))
                    .andExpect(status().isOk())
                    .andReturn();


        String responseContent = result.getResponse().getContentAsString();
        List<OrderDTO> responseDTOs = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class));

        assertEquals(topSellOrders, responseDTOs);
        verify(orderService, times(1)).getTopSellOrders();
    }

    @Test
    public void testGetTopBuyOrders_whenNoOrders_thenReturnsEmptyList() throws Exception {
        when(orderService.getTopBuyOrders()).thenReturn(CompletableFuture.completedFuture(List.of()));

        MvcResult result = mockMvc.perform(asyncDispatch(
                    mockMvc.perform(get("/api/orders/top10/buy"))
                            .andExpect(status().isOk())
                            .andReturn()))
                    .andExpect(status().isOk())
                    .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<OrderDTO> responseDTOs = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class));

        assertTrue(responseDTOs.isEmpty());
        verify(orderService, times(1)).getTopBuyOrders();
    }

    @Test
    public void testGetTopSellOrders_whenNoOrders_thenReturnsEmptyList() throws Exception {
        when(orderService.getTopSellOrders()).thenReturn(CompletableFuture.completedFuture(List.of()));

        MvcResult result = mockMvc.perform(asyncDispatch(
                    mockMvc.perform(get("/api/orders/top10/sell"))
                            .andExpect(status().isOk())
                            .andReturn()))
                    .andExpect(status().isOk())
                    .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<OrderDTO> responseDTOs = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class));

        assertTrue(responseDTOs.isEmpty());
        verify(orderService, times(1)).getTopSellOrders();
    }
}
