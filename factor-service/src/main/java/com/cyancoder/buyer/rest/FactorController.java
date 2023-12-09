package com.cyancoder.buyer.rest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/factor")
public class FactorController {



    @GetMapping("/factorInfo")
    public String getInfo(){
        return "its work";
    }
}
