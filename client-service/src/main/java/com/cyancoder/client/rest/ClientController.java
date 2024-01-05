package com.cyancoder.client.rest;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/client-service/clients1")
public class ClientController {


    @GetMapping
    public String getInfo(){
        return "its work";
    }



//    @PostMapping
//    public String addClient(@RequestBody CreateClientReqModel request){
//
//    }

}
