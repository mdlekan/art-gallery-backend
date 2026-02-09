package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.dto.CustomerDTO;
import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.repository.CustomerRespository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerService
{
	private final CustomerRespository customerRespository;

	public CustomerService(CustomerRespository customerRespository) {
		this.customerRespository = customerRespository;
	}

	public Customer findCustomerById(Long inCustomerId)
	{
		return customerRespository.findCustomerById(inCustomerId);
	}

	public Customer findCustomerByEmail(String inCustomerEmail)
	{
		return customerRespository.findCustomerByEmail(inCustomerEmail)
				.orElseThrow(() -> new RuntimeException("Customer Not found"));
	}

	public List<Customer> findAllCustomers()
	{
		return customerRespository.findAll();
	}

	public CustomerDTO addCustomer(CustomerDTO inCustomerDTO)
	{
		Customer customer = Customer.builder().email(inCustomerDTO.getEmail())
				.addressLine1(inCustomerDTO.getAddressLine1()).city(inCustomerDTO.getCity())
				.state(inCustomerDTO.getState()).lastName(inCustomerDTO.getLastName())
				.firstName(inCustomerDTO.getFirstName()).zipCode(inCustomerDTO.getZipCode())
				.emailOptIn(inCustomerDTO.getEmailOptIn()).email(inCustomerDTO.getEmail()).build();

		customerRespository.save(customer);

		return mapToResponse(customer);
	}

	private CustomerDTO mapToResponse(Customer inCustomer)
	{

		return CustomerDTO.builder().firstName(inCustomer.getFirstName()).lastName(inCustomer.getLastName())
				.addressLine1(inCustomer.getAddressLine1()).addressLine2(inCustomer.getAddressLine2())
				.city(inCustomer.getCity()).zipCode(inCustomer.getZipCode()).email(inCustomer.getEmail())
				.emailOptIn(inCustomer.getEmailOptIn()).build();
	}
}
