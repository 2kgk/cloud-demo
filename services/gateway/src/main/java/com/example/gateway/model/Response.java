package com.example.gateway.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Response {
    private int code;
    private String message;
    private Map<String, Object> data;

    public static Response success() {
        Response resp = new Response();
        resp.setCode(200);
        resp.setMessage("Success");
        resp.setData(new HashMap<>());
        return resp;
    }

    public static Response error(String message) {
        Response resp = new Response();
        resp.setCode(401);
        resp.setMessage(message);
        return resp;
    }
}
