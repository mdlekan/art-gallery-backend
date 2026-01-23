package com.mikelekan.artgallery.repository;

import com.mikelekan.artgallery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data Magic: This automatically creates the correct SQL
    // based on your field name "userName"
    Optional<User> findByUserName(String userName);
}