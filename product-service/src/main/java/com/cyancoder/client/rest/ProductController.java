package com.cyancoder.client.rest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/product")
public class ProductController {


    @GetMapping
    public String getInfo(){
        return "its work";
    }
}
