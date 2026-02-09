package com.mikelekan.artgallery.repository;

import com.mikelekan.artgallery.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRespository extends JpaRepository<Customer, Long>
{
	Customer findCustomerById(Long inCustomerId);

	Optional<Customer> findCustomerByEmail(String inEmail);
}
