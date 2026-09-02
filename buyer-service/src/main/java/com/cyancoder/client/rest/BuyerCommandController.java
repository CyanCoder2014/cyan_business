package com.cyancoder.client.rest;

import com.cyancoder.client.model.BuyerModel;
import com.cyancoder.client.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/buyer-service/buyers")
@RequiredArgsConstructor
public class BuyerCommandController {

    private final BuyerService buyerService;

    /** Returns the buyer id so a caller such as factor-service can reference it. */
    @PostMapping
    public String createBuyer(@RequestBody BuyerModel request) {
        return buyerService.addOrEdit(request);
    }
}
