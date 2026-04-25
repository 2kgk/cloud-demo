package com.example.order.controller;

import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 根据ID查询订单（包含产品详情）
     * @param id 订单ID
     * @return 订单信息（包含产品详情）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderByIdWithProductDetails(@PathVariable Long id) {
        Order order = orderService.getOrderByIdWithProductDetails(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取所有订单（包含产品详情）
     * @return 所有订单列表（包含产品详情）
     */
    @GetMapping("/all")
    public ResponseEntity<Map<Long, Order>> getAllOrdersWithProductDetails() {
        Map<Long, Order> orders = orderService.getAllOrdersWithProductDetails();
        return ResponseEntity.ok(orders);
    }
}