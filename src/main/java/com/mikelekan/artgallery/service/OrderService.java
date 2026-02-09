package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.dto.OrderRequest;
import com.mikelekan.artgallery.dto.OrderResponse;
import com.mikelekan.artgallery.exception.AlreadySoldException;
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

		// 2. Check if artwork is already sold
		if (artwork.isSold())
			// This immediately jumps to the GlobalExceptionHandler!
			throw new AlreadySoldException("Sorry, this masterpiece has already been purchased.");

		Customer customer = customerRepository.findCustomerByEmail(request.getCustomerEmail())
				.orElseGet(() -> {
					// If they don't exist, create a new "Guest" record
					Customer newCustomer = new Customer();
					newCustomer.setEmail(request.getCustomerEmail());
					newCustomer.setLastName(request.getCustomerName());
					return customerRepository.save(newCustomer);
				});

		// 3. Create order
		Order order = Order.builder()
				.artwork(artwork)
				.customer(customer)
				.customerName(request.getCustomerName())
				.customerEmail(request.getCustomerEmail())
				// ... set address fields from request ...
				.price(artwork.getPrice()) // <-- CRITICAL: Get price from DB, not JSON
				.status(OrderStatus.PENDING)
				.build();

		Order savedOrder = orderRepository.save(order);

		// 4. Mark artwork as sold (after payment confirmation in real app)
		// For now, we'll do it immediately
		artwork.setSold(true);
		artWorkRepository.save(artwork);

		try
		{
			String clientSecret = paymentService.createPaymentIntent(savedOrder);
			savedOrder.setPaymentIntentId(clientSecret);
		}
		catch (StripeException e)
		{
			// Log log = new Log
			// log.error("Failed to create payment intent", e);
			throw new RuntimeException("Payment initialization failed");
		}

		// 5. Send confirmation email (implement later)
		// emailService.sendOrderConfirmation(savedOrder);

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

	public void markAsPaid(Long orderId, String paymentIntentId)
	{
		Order order = orderRepository.findById(orderId).orElseThrow(() ->
				new EntityNotFoundException("Order not found"));

		order.setStatus(OrderStatus.PAID);
	}
}
