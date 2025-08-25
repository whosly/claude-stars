package com.yueny.stars.netty.visualizer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web页面控制器
 * 
 * @author fengyang
 */
@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/channels")
    public String channels() {
        return "channels";
    }
    
    @GetMapping("/eventloops")
    public String eventLoops() {
        return "eventloops";
    }
    
    @GetMapping("/performance")
    public String performance() {
        return "performance";
    }
    
    @GetMapping("/errors")
    public String errors() {
        return "errors";
    }
}