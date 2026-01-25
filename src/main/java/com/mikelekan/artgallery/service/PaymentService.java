package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@Slf4j  // Lombok logging
public class PaymentService {

    @Value("${stripe_secret_key}")
    private String secretKey;

    @Value("${stripe.currency:usd}")
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe API initialized");
    }

    public String createPaymentIntent(Order order) throws StripeException {
        // Validation
        validateOrder(order);

        log.info("Creating payment intent for order ID: {}, amount: ${}",
                order.getId(), order.getPrice());

        // Convert to cents
        long amountInCents = order.getPrice()
                .multiply(new BigDecimal(100))
                .longValue();

        // Build payment intent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setDescription("Art Gallery Order #" + order.getId())
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("customer_email", order.getCustomerEmail())
                .putMetadata("customer_name", order.getCustomerName())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        // Create with idempotency key to prevent duplicate charges
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey("order_" + order.getId())
                .build();

        PaymentIntent intent = PaymentIntent.create(params, requestOptions);

        log.info("Payment intent created successfully: {}", intent.getId());

        return intent.getClientSecret();
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.getId() == null) {
            throw new IllegalArgumentException("Order must have an ID before creating payment intent");
        }

        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order price must be greater than zero");
        }

        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Order must have customer email");
        }
    }
}