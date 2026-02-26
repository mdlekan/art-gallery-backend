package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.dto.OrderRequest;
import com.mikelekan.artgallery.dto.OrderResponse;
import com.mikelekan.artgallery.exception.BusinessLogicException;
import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.model.Order;
import com.mikelekan.artgallery.model.OrderStatus;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import com.mikelekan.artgallery.repository.CustomerRespository;
import com.mikelekan.artgallery.repository.OrderRepository;
import com.mikelekan.artgallery.service.vendors.PaymentService;
import com.stripe.exception.StripeException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService
{
	private final OrderRepository orderRepository;
	private final ArtWorkRepository artWorkRepository;
	private final PaymentService paymentService;
	private final CustomerRespository customerRepository;

	// private final EmailService emailService; // Add later for confirmations
	@Transactional
	public OrderResponse createOrder(OrderRequest request) throws StripeException
	{
		if (request.getArtworkId() == null)
		{
			throw new IllegalArgumentException("Order must contain at least one artwork");
		}
		// 1. Find artwork
		ArtWork artwork = artWorkRepository.findById(request.getArtworkId())
				.orElseThrow(() -> new RuntimeException("Artwork not found"));

		Customer customer = customerRepository.findCustomerByEmail(request.getCustomerEmail())
				.orElseGet(() -> {
					Customer newCustomer = new Customer();
					newCustomer.setEmail(request.getCustomerEmail());
					newCustomer.setFirstName(""); // Split name if needed
					newCustomer.setLastName(request.getCustomerName());
					// IMPORTANT: If Customer entity requires address, set it here!
					return customerRepository.save(newCustomer);
				});

		Order order = Order.builder()
				.artwork(artwork)
				.customer(customer)
				.customerName(request.getCustomerName())
				.customerEmail(request.getCustomerEmail())
				// Map these address fields to avoid nullable errors!
				.shippingAddressLine1(request.getShippingAddressLine1())
				.city(request.getCity())
				.state(request.getState())
				.postalCode(request.getPostalCode())
				.country(request.getCountry())
				.price(artwork.getPrice())
				.status(OrderStatus.PENDING)
				.build();

		Order savedOrder = orderRepository.save(order);

		// Initializing Stripe
		try {
			// You might want to return a DTO from paymentService
			// that contains BOTH the ID and the Secret
			String clientSecret = paymentService.createPaymentIntent(savedOrder);

			// Extract the ID (pi_...) from the secret to save in DB
			String paymentIntentId = clientSecret.split("_secret")[0];
			savedOrder.setPaymentIntentId(paymentIntentId);

			// You'll need to update OrderResponse to include the clientSecret
			// so the frontend can actually use it!
		} catch (StripeException e) {
			throw new BusinessLogicException("Could not initialize payment: " + e.getMessage());
		}

		return mapToResponse(savedOrder);
	}

	public List<OrderResponse> getAllOrders()
	{
		return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public OrderResponse updateOrderStatus(Long orderId, String statusStr)
	{
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
		order.setStatus(status);
		Order updatedOrder = orderRepository.save(order);

		return mapToResponse(updatedOrder);
	}

	private OrderResponse mapToResponse(Order order)
	{
		return OrderResponse.builder()
				.id(order.getId())
				.artworkId(order.getArtwork().getId())
				.artworkTitle(order.getArtwork().getTitle())
				.artworkImageUrl(order.getArtwork().getImageUrl())
				.customerName(order.getCustomerName()).customerEmail(order.getCustomerEmail()).price(order.getPrice())
				.status(order.getStatus()).paymentIntentId(order.getPaymentIntentId()).createdAt(order.getCreatedAt())
				.build();
	}

	public void markAsPaid(Long orderId)
	{
		Order order = orderRepository.findById(orderId).orElseThrow(() ->
				new EntityNotFoundException("Order not found"));

		order.setStatus(OrderStatus.PAID);
	}
}
