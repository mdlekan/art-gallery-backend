package com.mikelekan.artgallery.model;

public enum OrderStatus {
    PENDING,      // Payment initiated but not confirmed
    PAID,         // Payment successful
    SHIPPED,      // Artwork shipped to customer
    DELIVERED,    // Customer received artwork
    CANCELLED     // Order cancelled
}