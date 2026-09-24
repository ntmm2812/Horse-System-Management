package com.horsemanagement.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping(value = "/api/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test() {
        return "Horse Management Backend is running";
    }
}
