package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.dto.OrderRequest;
import com.mikelekan.artgallery.dto.OrderResponse;
import com.mikelekan.artgallery.service.OrderService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Public endpoint - customers can place orders
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) throws StripeException {
        OrderResponse order = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(order);
    }

    // Admin endpoint - view all orders (requires authentication)
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Admin endpoint - update order status (requires authentication)
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
}