package com.mikelekan.artgallery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        // Verify webhook signature
        // Update order status when payment succeeds
        // Mark artwork as sold

        return ResponseEntity.ok().build();
    }
}