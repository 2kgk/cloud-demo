package com.example.order.service;

import com.example.order.client.ProductClient;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务类
 */
@Service
public class OrderService {
    
    @Autowired
    private ProductClient productClient;
    
    // 使用内存存储订单数据，不使用数据库
    private static final Map<Long, Order> orderMap = new HashMap<>();
    
    static {
        // 初始化一些测试数据
        List<Long> productIds1 = new ArrayList<>();
        productIds1.add(1L);
        productIds1.add(2L);
        orderMap.put(1L, new Order(1L, "ORDER-2026001", productIds1, 18998.0, "COMPLETED"));
        
        List<Long> productIds2 = new ArrayList<>();
        productIds2.add(3L);
        orderMap.put(2L, new Order(2L, "ORDER-2026002", productIds2, 1999.0, "PROCESSING"));
    }
    
    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单信息（包含产品详情）
     */
    public Order getOrderByIdWithProductDetails(Long id) {
        Order order = orderMap.get(id);
        if (order != null) {
            // 获取产品详情
            order.setProductIds(new ArrayList<>(order.getProductIds())); // 创建副本
            
            // 计算总价（实际应用中可能不需要，这里只是演示）
            double totalPrice = 0.0;
            for (Long productId : order.getProductIds()) {
                Product product = productClient.getProductById(productId);
                if (product != null) {
                    totalPrice += product.getPrice();
                }
            }
            order.setTotalPrice(totalPrice);
        }
        return order;
    }
    
    /**
     * 获取所有订单
     * @return 所有订单列表（包含产品详情）
     */
    public Map<Long, Order> getAllOrdersWithProductDetails() {
        for (Order order : orderMap.values()) {
            // 获取产品详情
            List<Long> productDetails = new ArrayList<>();
            double totalPrice = 0.0;
            
            for (Long productId : order.getProductIds()) {
                Product product = productClient.getProductById(productId);
                if (product != null) {
                    productDetails.add(productId);
                    totalPrice += product.getPrice();
                }
            }
            
            order.setProductIds(productDetails);
            order.setTotalPrice(totalPrice);
        }
        return orderMap;
    }
}