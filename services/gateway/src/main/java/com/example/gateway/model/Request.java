package com.example.gateway.model;

import lombok.Data;

@Data
public class Request {
    private String username;
    private String password;
    private String refreshToken;
}
