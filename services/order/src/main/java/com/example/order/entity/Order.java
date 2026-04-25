package com.example.order.entity;

import java.util.List;

/**
 * 订单实体类
 */
public class Order {
    private Long id;
    private String orderNo;
    private List<Long> productIds;
    private Double totalPrice;
    private String status;
    
    public Order() {}
    
    public Order(Long id, String orderNo, List<Long> productIds, Double totalPrice, String status) {
        this.id = id;
        this.orderNo = orderNo;
        this.productIds = productIds;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public List<Long> getProductIds() {
        return productIds;
    }
    
    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}