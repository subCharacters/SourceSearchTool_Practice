package com.example.sourcesearchtool_practice.contorller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloWorld {
    @GetMapping("/helloworld")
    public String helloWorld() {
        return "HelloWorld";
    }
}
