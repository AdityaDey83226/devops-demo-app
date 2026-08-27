package com.sap.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello from SAP DevOps Training! This is a devops demo";
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
