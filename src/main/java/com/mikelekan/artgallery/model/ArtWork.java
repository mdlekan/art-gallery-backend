package com.mikelekan.artgallery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "artworks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtWork
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String artist;

	private String imageUrl;

	private BigDecimal price;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private boolean sold = false;

	@OneToOne(mappedBy = "artwork")
	@JsonIgnore // Stop the infinite loop here!
	private Order order;

	@PrePersist
	protected void onCreate()
	{
		this.createdAt = LocalDateTime.now();
	}
}
