package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.model.Order;
import com.mikelekan.artgallery.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController
{

	private final String webhookSecret;
	private final OrderRepository orderRepository;

	public StripeWebhookController(@Value("${stripe_webhook_secret}") String webhookSecret,
			OrderRepository orderRepository) {
		this.webhookSecret = webhookSecret;
		this.orderRepository = orderRepository;
	}

	@PostMapping("/stripe")
	public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sigHeader)
	{

		Event event;

		try
		{
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (SignatureVerificationException e)
		{
			log.warn("Invalid Stripe signature");
			return ResponseEntity.badRequest().build();
		}

		log.info("Received Stripe event: {}", event.getType());

		switch (event.getType()) {
			case "payment_intent.succeeded" -> {
				handlePaymentIntentSucceeded(event);
			}

			case "payment_intent.payment_failed" -> {
				handlePaymentIntentFailed(event);
			}

			default -> {
				log.debug("Unhandled event type: {}", event.getType());
			}
		}

		return ResponseEntity.ok("Received");
	}

	private void handlePaymentIntentSucceeded(Event event)
	{
		PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();

		String orderId = intent.getMetadata().get("order_id");

		if (orderId == null)
		{
			log.error("PaymentIntent {} missing order_id metadata", intent.getId());
			return;
		}

		Order order = orderRepository.findById(Long.valueOf(orderId))
				.orElseThrow(() -> new IllegalStateException("Order not found"));

		if (order.getIsPaid())
		{
			log.info("Order {} already marked paid (idempotent)", orderId);
			return;
		}

		order.setMarkPaid(intent.getId());
		orderRepository.save(order);

		log.info("Order {} marked as PAID", orderId);
	}

	private void handlePaymentIntentFailed(Event event)
	{
		PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();

		log.warn("Payment failed for intent {}", intent.getId());
	}
}
