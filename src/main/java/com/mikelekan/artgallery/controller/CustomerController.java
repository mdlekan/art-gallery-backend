package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.dto.CustomerDTO;
import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor  // ← Lombok generates constructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.findCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping  // Using query param: /api/customers?email=john@example.com
    public ResponseEntity<Customer> getCustomerByEmail(@RequestParam String email) {
        Customer customer = customerService.findCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
            @Valid @RequestBody CustomerDTO customerDTO) {
        System.out.println("========================================");
        System.out.println("CONTROLLER REACHED!");
        System.out.println("Email received: " + customerDTO.getEmail());
        System.out.println("========================================");
        CustomerDTO created = customerService.addCustomer(customerDTO);
        return ResponseEntity.ok(created);
    }
}