package com.keqing.webterminal.websocket.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author keqing
 */
@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @Resource
    private ProxyService proxyService;

//    @PostMapping("/**")
//    public void proxyPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestBody(required = false) String body) throws Exception {
//        proxyService.proxy(httpServletRequest, httpServletResponse, body, HttpMethod.POST);
//    }

    @GetMapping({"", "/token"})
    public void proxyGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        if (!httpServletRequest.getRequestURI().endsWith("/ws")) {
            proxyService.proxy(httpServletRequest, httpServletResponse, null, HttpMethod.GET);
        }
    }

    // similar for other HTTP methods like PUT, DELETE ......
}