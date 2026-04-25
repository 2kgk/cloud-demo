package com.example.order.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Author: 11
 * Date:2026/3/17-03-17-19:06
 * Description:com.example.order.test
 * version: 1.0
 */
@SpringBootTest
public class TestFind {
    @Autowired
    DiscoveryClient discoveryClient;

    @Test
    void find() {
        for (String service : discoveryClient.getServices()) {
            System.out.println("service=" + service);
        }
    }
}
