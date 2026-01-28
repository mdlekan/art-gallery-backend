package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.model.Order;
import com.mikelekan.artgallery.repository.OrderRepository;
import com.mikelekan.artgallery.service.vendors.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempOderCheckoutController
{
	private final OrderRepository orderRepository;
	private final PaymentService paymentService;

	public TempOderCheckoutController(OrderRepository orderRepository, PaymentService paymentService) {
		this.orderRepository = orderRepository;
		this.paymentService = paymentService;
	}

	@PostMapping("/orders/{id}/checkout")
	public ResponseEntity<String> startCheckout(@PathVariable Long id) throws StripeException
	{

		Order order = orderRepository.findById(id).orElseThrow();

		String checkoutUrl = paymentService.createCheckoutSession(order);

		return ResponseEntity.ok(checkoutUrl);
	}
}
