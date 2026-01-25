package com.mikelekan.artgallery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO
{
    @NotBlank(message = "First Name must be provided")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last Name must be provided")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Street Address must be provided")
    @Size(min = 2, max = 100)
    private String addressLine1;

    @Size(min = 2, max = 100)
    private String addressLine2;


    @Size(min = 2, max = 50)
    @NotBlank(message = "City must be provided")
    private String city;


    @Size(min = 2, max = 50)
    @NotBlank(message = "State must be provided")
    private String state;


    @Size(min = 2, max = 5)
    @NotBlank(message = "Zip Code must provided")
    private String zipCode;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must be provided")
    private String email;

    private Boolean emailOptIn;
}
