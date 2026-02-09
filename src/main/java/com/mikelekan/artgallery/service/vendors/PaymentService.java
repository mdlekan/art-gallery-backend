package com.mikelekan.artgallery.service.vendors;

import com.mikelekan.artgallery.model.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import static java.rmi.server.LogStream.log;

@Service
@Slf4j
public class PaymentService {

    private final String stripeSecretKey;
    private final String currency;

    public PaymentService(
            @Value("${stripe_secret_key}") String stripeSecretKey,
            @Value("${stripe.currency:usd}") String currency
    ) {
        this.stripeSecretKey = stripeSecretKey;
        this.currency = currency;
    }

    public String createPaymentIntent(Order order) throws StripeException {
        validateOrder(order);

        PaymentService.log.info("Creating payment intent for order ID: {}, amount: ${}",
                order.getId(), order.getPrice());

        long amountInCents = order.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(currency)
                        .setDescription("Art Gallery Order #" + order.getId())
                        .putMetadata("order_id", order.getId().toString())
                        .putMetadata("customer_email", order.getCustomerEmail())
                        .putMetadata("customer_name", order.getCustomerName())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setApiKey(stripeSecretKey)
                        .setIdempotencyKey("order_" + order.getId())
                        .build();

        PaymentIntent intent =
                PaymentIntent.create(params, requestOptions);

        PaymentService.log.info("Payment intent created successfully: {}", intent.getId());

        return intent.getClientSecret(); //Send to front end so front end can communicate the order to Stripe.
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

    //This method is temporary for checking out a session to simulate a payment to Stripe.
    public String createCheckoutSession(Order order) throws StripeException {

        validateOrder(order);

        log("session created");
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(
                                                                order.getPrice()
                                                                        .multiply(BigDecimal.valueOf(100))
                                                                        .longValueExact()
                                                        )
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData
                                                                        .builder()
                                                                        .setName("Artwork Order #" + order.getId())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .putMetadata("order_id", order.getId().toString())
                        .build();

        Session session = Session.create(params);

        return session.getUrl(); // <-- this is the magic
    }
}
