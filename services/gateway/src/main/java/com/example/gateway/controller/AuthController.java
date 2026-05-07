package com.example.gateway.controller;

import com.example.gateway.model.Request;
import com.example.gateway.model.Response;
import com.example.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** 回调：远程认证登录后重定向回来，用 code 换取本地 token */
    @GetMapping("/callback")
    public Mono<Response> callback(@RequestParam String code, ServerHttpResponse response) {
        return authService.exchangeCode(code, response);
    }

    /** 刷新本地 token */
    @PostMapping("/refresh")
    public Mono<Response> refresh(@RequestBody Request request, ServerHttpResponse response) {
        return authService.refresh(request.getRefreshToken(), response);
    }
}
