package com.mikelekan.artgallery.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer
{
	@OneToMany(mappedBy = "customer")
	private List<Order> orders;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false)
	private String addressLine1;

	private String addressLine2;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(nullable = false)
	private String zipCode;

	@Column(nullable = false, unique = true)
	private String email;

	private Boolean emailOptIn;
}
