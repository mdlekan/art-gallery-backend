package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.repository.CustomerRespository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService
{
    private final CustomerRespository customerRespository;

    public CustomerService(CustomerRespository customerRespository)
    {
        this.customerRespository = customerRespository;
    }

    public Customer findCustomerById(Long inCustomerId)
    {
        return customerRespository.findCustomerById(inCustomerId);
    }

    public Customer findCustomerByEmail(String inCustomerEmail)
    {
        return customerRespository.findCustomerByEmail(inCustomerEmail);
    }

    public List<Customer> findAllCustomers()
    {
        return customerRespository.findAll();
    }

    public void addCustomer(Customer inCustomer)
    {
        customerRespository.save(inCustomer);
    }
}
