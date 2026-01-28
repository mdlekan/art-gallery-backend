package com.mikelekan.artgallery.repository;

import com.mikelekan.artgallery.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>
{
	List<Order> findByCustomerEmailOrderByCreatedAtDesc(String email);

	List<Order> findAllByOrderByCreatedAtDesc();
}
