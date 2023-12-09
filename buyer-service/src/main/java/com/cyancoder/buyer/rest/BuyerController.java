package com.cyancoder.buyer.rest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buyerInfo")
public class BuyerController {



    @GetMapping
    public String getInfo(){
        return "its work";
    }
}
