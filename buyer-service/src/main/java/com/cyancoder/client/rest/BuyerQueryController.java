package com.cyancoder.client.rest;

import com.cyancoder.client.model.BuyerModel;
import com.cyancoder.client.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/buyer-service/buyers")
@RequiredArgsConstructor
public class BuyerQueryController {

    private final BuyerService buyerService;

    @GetMapping
    public List<BuyerModel> listBuyers() {
        return buyerService.findAll();
    }

    /**
     * Lookup by id. The previous version had no such route — the only GET took
     * a request body and ignored it — so factor-service had to reach for a
     * distributed query instead of an HTTP call.
     */
    @GetMapping("/{buyerId}")
    public ResponseEntity<BuyerModel> getBuyer(@PathVariable String buyerId) {
        BuyerModel buyer = buyerService.findById(buyerId);
        return buyer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(buyer);
    }
}
