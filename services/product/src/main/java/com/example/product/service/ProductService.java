package com.example.product.service;

import com.example.product.entity.Product;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 产品服务类
 */
@Service
public class ProductService {
    
    // 使用内存存储产品数据，不使用数据库
    private static final Map<Long, Product> productMap = new HashMap<>();
    
    static {
        // 初始化一些测试数据
        productMap.put(1L, new Product(1L, "iPhone 13", "苹果iPhone 13智能手机", 5999.0));
        productMap.put(2L, new Product(2L, "MacBook Pro", "苹果MacBook Pro笔记本电脑", 12999.0));
        productMap.put(3L, new Product(3L, "AirPods Pro", "苹果AirPods Pro无线耳机", 1999.0));
    }
    
    /**
     * 根据ID查询产品
     * @param id 产品ID
     * @return 产品信息
     */
    public Product getProductById(Long id) {
        return productMap.get(id);
    }
    
    /**
     * 获取所有产品
     * @return 所有产品列表
     */
    public Map<Long, Product> getAllProducts() {
        return productMap;
    }
}