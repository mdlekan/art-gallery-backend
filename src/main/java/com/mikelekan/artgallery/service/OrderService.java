package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.dto.OrderRequest;
import com.mikelekan.artgallery.dto.OrderResponse;
import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.model.Order;
import com.mikelekan.artgallery.model.OrderStatus;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import com.mikelekan.artgallery.repository.OrderRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ArtWorkRepository artWorkRepository;
    private final PaymentService paymentService;
    // private final EmailService emailService;  // Add later for confirmations

    @Transactional
    public OrderResponse createOrder(OrderRequest request) throws StripeException {

        if (request.getArtworkId() == null)
        {
            throw new IllegalArgumentException("Order must contain at least one artwork");
        }
        // 1. Find artwork
        ArtWork artwork = artWorkRepository.findById(request.getArtworkId())
                .orElseThrow(() -> new RuntimeException("Artwork not found"));

        // 2. Check if artwork is already sold
        if (artwork.isSold()) {
            throw new RuntimeException("Artwork is already sold");
        }

        // 3. Create order
        Order order = Order.builder()
                .artwork(artwork)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .price(artwork.getPrice())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 4. Mark artwork as sold (after payment confirmation in real app)
        // For now, we'll do it immediately
        artwork.setSold(true);
        artWorkRepository.save(artwork);

        try{
            String clientSecret = paymentService.createPaymentIntent(savedOrder);
            savedOrder.setPaymentIntentId(clientSecret);
        }
        catch (StripeException e)
        {
//            Log log = new Log
//            log.error("Failed to create payment intent", e);
            throw new RuntimeException("Payment initialization failed");
        }

        // 5. Send confirmation email (implement later)
        // emailService.sendOrderConfirmation(savedOrder);

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .artworkId(order.getArtwork().getId())
                .artworkTitle(order.getArtwork().getTitle())
                .artworkImageUrl(order.getArtwork().getImageUrl())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .price(order.getPrice())
                .status(order.getStatus())
                .paymentIntentId(order.getPaymentIntentId())
                .createdAt(order.getCreatedAt())
                .build();
    }
}