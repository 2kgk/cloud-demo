package com.example.gateway.service;

import com.example.gateway.model.Response;
import com.example.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final WebClient webClient;
    /** 本地 refresh_token -> 用户名 的存储 */
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @Value("${auth.remote.base-url}")
    private String remoteAuthBaseUrl;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.webClient = WebClient.builder().build();
    }

    /** 用远程返回的 code 换取远程 token，再获取用户信息，最后生成本地 token */
    public Mono<Response> exchangeCode(String code, ServerHttpResponse response) {
        // 1. 调用远程 /api/auth/token?code=xxx 获取远程 access_token
        return webClient.get()
                .uri(remoteAuthBaseUrl + "/api/auth/token?code={code}", code)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(tokenResp -> {
                    // 尝试从 data 字段中提取 access_token
                    Map<String, Object> tokenData = extractData(tokenResp);
                    String remoteToken = tokenData != null
                            ? (String) tokenData.get("access_token")
                            : null;
                    if (remoteToken == null) {
                        return Mono.just(Response.error("Failed to get remote token"));
                    }
                    // 2. 用远程 token 获取用户信息
                    return fetchUserInfo(remoteToken, response);
                })
                .onErrorResume(e -> Mono.just(Response.error("Remote auth error: " + e.getMessage())));
    }

    /** 调用远程 /api/auth/user-info 获取用户名 */
    private Mono<Response> fetchUserInfo(String remoteToken, ServerHttpResponse response) {
        return webClient.get()
                .uri(remoteAuthBaseUrl + "/api/auth/user-info")
                .header("Authorization", "Bearer " + remoteToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(userResp -> {
                    Map<String, Object> userData = extractData(userResp);
                    String userId = userData != null ? (String) userData.get("userId") : null;
                    if (userId == null) {
                        return Response.error("Failed to get user info");
                    }
                    // 3. 生成本地 token 并设置到 HttpOnly Cookie
                    String localToken = jwtUtil.generateToken(userId);
                    String localRefreshToken = jwtUtil.generateRefreshToken();
                    refreshTokenStore.put(localRefreshToken, userId);
                    setTokenCookies(response, localToken, localRefreshToken);
                    return Response.success();
                });
    }

    private void setTokenCookies(ServerHttpResponse response, String token, String refreshToken) {
        response.addCookie(ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build());
        response.addCookie(ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build());
    }

    /** 从远程统一响应中提取 data 字段 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> resp) {
        Object data = resp.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    /** 用 refresh_token 刷新 access_token */
    public Mono<Response> refresh(String refreshToken, ServerHttpResponse response) {
        String username = refreshTokenStore.get(refreshToken);
        if (username == null) {
            return Mono.just(Response.error("Invalid or expired refresh token"));
        }
        String newToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken();
        refreshTokenStore.remove(refreshToken);
        refreshTokenStore.put(newRefreshToken, username);
        setTokenCookies(response, newToken, newRefreshToken);
        return Mono.just(Response.success());
    }

    /** 校验本地 token 是否有效 */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
