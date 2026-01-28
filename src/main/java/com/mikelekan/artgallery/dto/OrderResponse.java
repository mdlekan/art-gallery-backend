package com.mikelekan.artgallery.dto;

import com.mikelekan.artgallery.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse
{
	private Long id;
	private Long artworkId;
	private String artworkTitle;
	private String artworkImageUrl;
	private String customerName;
	private String customerEmail;
	private BigDecimal price;
	private OrderStatus status;
	private String paymentIntentId;
	private LocalDateTime createdAt;
}
