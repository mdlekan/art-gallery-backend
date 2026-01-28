package com.mikelekan.artgallery.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "artwork_id", nullable = false)
	private ArtWork artwork;

	@ManyToOne
	@JoinColumn(name = "customer_id") // ← This creates customer_id column in orders table
	private Customer customer;

	// Customer info
	@Column(nullable = false)
	private String customerName;

	@Column(nullable = false)
	private String customerEmail;

	private String customerPhone;

	// Shipping address
	@Column(nullable = false)
	private String shippingAddressLine1;

	private String shippingAddressLine2;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(nullable = false)
	private String postalCode;

	@Column(nullable = false)
	private String country;

	// Order details
	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private String paymentIntentId; // Stripe payment ID

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private Boolean isPaid;

	private String markPaid;

	@PrePersist
	protected void onCreate()
	{
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate()
	{
		updatedAt = LocalDateTime.now();
	}
}
