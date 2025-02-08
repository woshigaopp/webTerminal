package com.keqing.webterminal.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author keqing@date 2025/02/05@date 2025/02/05
 *
 */
@Controller
public class WebShellController {
    /**
     * web shell浏览器访问地址
     * @return {@link String} 返回前端页面
     */
    @RequestMapping("/web-shell-page")
    public String webShellPage(){
        return "webssh";
    }

}
