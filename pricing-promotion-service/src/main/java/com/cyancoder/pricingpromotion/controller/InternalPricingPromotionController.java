package com.cyancoder.pricingpromotion.controller;

import com.cyancoder.pricingpromotion.model.PricingEvaluationRequest;
import com.cyancoder.pricingpromotion.model.PricingEvaluationResponse;
import com.cyancoder.pricingpromotion.service.PricingEvaluationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/pricing-promotions")
public class InternalPricingPromotionController {
    private final PricingEvaluationService pricingEvaluationService;

    public InternalPricingPromotionController(PricingEvaluationService pricingEvaluationService) {
        this.pricingEvaluationService = pricingEvaluationService;
    }

    @PostMapping("/evaluate")
    public PricingEvaluationResponse evaluate(@RequestBody PricingEvaluationRequest request) {
        return pricingEvaluationService.evaluate(request);
    }
}
