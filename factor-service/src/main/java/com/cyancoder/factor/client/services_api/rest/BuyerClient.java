package com.cyancoder.factor.client.services_api.rest;

import com.cyancoder.factor.model.BuyerModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Replaces the Axon command and query gateways factor-service used to reach
 * buyer-service with. Same shape as {@link TaxClient}, so inter-service calls
 * here are all plain HTTP over service discovery.
 */
@FeignClient(name = "buyer-service")
public interface BuyerClient {

    /** Upserts the buyer and returns its id. */
    @PostMapping("/v2/api/buyer-service/buyers")
    String addOrEditBuyer(@RequestBody BuyerModel buyer);

    @GetMapping("/v2/api/buyer-service/buyers/{buyerId}")
    BuyerModel getBuyer(@PathVariable("buyerId") String buyerId);
}
