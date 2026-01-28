package com.mikelekan.artgallery.repository;

import com.mikelekan.artgallery.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRespository extends JpaRepository<Customer, Long>
{
	Customer findCustomerById(Long inCustomerId);

	Customer findCustomerByEmail(String inEmail);
}
