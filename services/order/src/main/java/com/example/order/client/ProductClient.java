package com.example.order.client;

import com.example.order.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 产品服务Feign客户端
 */
@FeignClient(name = "product", path = "/api/products")
public interface ProductClient {
    
    /**
     * 根据ID查询产品
     * @param id 产品ID
     * @return 产品信息
     */
    @GetMapping("/{id}")
    Product getProductById(@PathVariable Long id);
    
    /**
     * 获取所有产品
     * @return 所有产品列表
     */
    @GetMapping("/all")
    Map<Long, Product> getAllProducts();
}