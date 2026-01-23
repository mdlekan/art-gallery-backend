package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/customers")
public class CustomerController
{
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService)
    {
        this.customerService = customerService;
    }

    @GetMapping
    @RequestMapping("/{id}")
    public Customer getCustomer(@PathVariable Long inId)
    {
       return customerService.findCustomerById(inId);
    }
    @GetMapping
    public Customer getCustomerByEmail(@PathVariable String inEmail)
    {
        return customerService.findCustomerByEmail(inEmail);
    }

    @PostMapping
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer inCustomer)
    {
        customerService.addCustomer(inCustomer);

        return ResponseEntity.ok(inCustomer);
    }
}
