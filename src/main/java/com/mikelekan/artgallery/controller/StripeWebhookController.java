package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.stripe.model.PaymentIntent;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.checkout.Session;


@RestController
@RequestMapping("/api/webhooks") // Simplified path
@Slf4j
public class StripeWebhookController {

    private final OrderService orderService;

    // Fixed to match your app.properties exactly
    @Value("${stripe_webhook_secret}")
    private String endpointSecret;

    public StripeWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/stripe") // This makes the final URL: /api/webhooks/stripe
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("--- Webhook Hit! ---");
        // This will now print TRUE
        System.out.println("Secret is loaded: " + (endpointSecret != null));

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("Event verified: " + event.getType());

            // Handle the event
            if ("checkout.session.completed".equals(event.getType())) {
                // 1. Get the Session object instead of PaymentIntent
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

                if (session != null) {
                    // 2. Pull the order_id from the SESSION metadata
                    String orderIdString = session.getMetadata().get("order_id");

                    if (orderIdString != null) {
                        Long orderId = Long.parseLong(orderIdString);
                        System.out.println("💳 Checkout Success! Updating Order #" + orderId);

                        // 3. This finally hits your DB
                        orderService.markAsPaid(orderId);
                    } else {
                        System.err.println("⚠️ No order_id found in Checkout Session metadata!");
                    }
                }
            }

            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            System.err.println("⚠️ Signature verification failed! Check your whsec key.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            System.err.println("❌ Internal Error:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}