package com.monitor.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để serve giao diện web
 */
@Controller
public class WebController {
    
    /**
     * Trang chủ - redirect đến /server
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/server.html";
    }
    
    /**
     * Giao diện quản lý tại /server - redirect đến server.html
     */
    @GetMapping("/server")
    public String server() {
        return "redirect:/server.html";
    }
}

