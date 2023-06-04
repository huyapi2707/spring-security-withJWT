package com.example.demospringsercurity.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/demo")

public class Demo {
    @GetMapping("/demo1")
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("Hello World!!");
    }
}
