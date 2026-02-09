package com.mikelekan.artgallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest
{
	private Long artworkId;
	private Long customerId;

	// Customer info
	private String customerName;
	private String customerEmail;
	private String customerPhone;

	// Shipping address
	private String shippingAddressLine1;
	private String shippingAddressLine2;
	private String city;
	private String state;
	private String postalCode;
	private String country;
}
