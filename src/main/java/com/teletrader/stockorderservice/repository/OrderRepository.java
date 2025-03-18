package com.teletrader.stockorderservice.repository;

import com.teletrader.stockorderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        WITH OrderedOrders AS (
            SELECT *,
                   SUM(remaining_amount) OVER (ORDER BY price ASC, creation_time ASC) AS running_total
            FROM orders
            WHERE order_status = 'ACTIVE' AND order_type = 'SELL' AND price <= :price
        )
        SELECT *
        FROM OrderedOrders
        WHERE running_total - remaining_amount < :amount
        ORDER BY price ASC, creation_time ASC;
        """, nativeQuery = true)
    List<Order> findMatchingSellOrders(@Param("price") BigDecimal price, @Param("amount") int amount);



    @Query(value = """
        WITH OrderedOrders AS (
            SELECT *,
                   SUM(remaining_amount) OVER (ORDER BY price ASC, creation_time ASC) AS running_total
            FROM orders
            WHERE order_status = 'ACTIVE' AND order_type = 'BUY' AND price >= :price
        )
        SELECT *
        FROM OrderedOrders
        WHERE running_total - remaining_amount < :amount
        ORDER BY price ASC, creation_time ASC;
        """, nativeQuery = true)
    List<Order> findMatchingBuyOrders(@Param("price") BigDecimal price, @Param("amount") int amount);

    @Query(value = """
        SELECT * FROM orders
        WHERE order_status = 'ACTIVE'
        AND order_type = 'BUY'
        ORDER BY price DESC, creation_time ASC
        LIMIT 10
        """, nativeQuery = true)
    List<Order> findTopBuyOrders();


    @Query(value = """
        SELECT * FROM orders
        WHERE order_status = 'ACTIVE'
        AND order_type = 'SELL'
        ORDER BY price ASC, creation_time ASC
        LIMIT 10
        """, nativeQuery = true)
    List<Order> findTopSellOrders();

}

