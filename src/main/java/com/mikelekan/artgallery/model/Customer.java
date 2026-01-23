package com.mikelekan.artgallery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

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
    @NotBlank(message = "First Name must be provided")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "Last Name must be provided")
    private String lastName;

    @Column(nullable = false)
    @NotBlank(message = "Street Address must be provided")
    private String addressLine1;

    private String addressLine2;

    @Column(nullable = false)
    @NotBlank(message = "City must be provided")
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "State must be provided")
    private String state;

    @Column(nullable = false)
    @NotBlank(message = "Zip Code must provided")
    private String zipCode;

    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email must be provided")
    private String email;

    private Boolean emailOptIn;
}
