 package com.mikelekan.artgallery.config;

 import com.mikelekan.artgallery.model.Role;
 import com.mikelekan.artgallery.model.User;
 import com.mikelekan.artgallery.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.CommandLineRunner;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.crypto.password.PasswordEncoder;

 @Configuration
 public class DataInitializer {

     @Bean
     CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
                                 @Value("${ADMIN_USERNAME}") String username,
                                 @Value("${ADMIN_PASSWORD}") String password)
     {
         return args -> {
             User admin = users.findByUserName(username)
                     .orElseGet(() -> User.builder()
                             .userName(username)
                             .password(encoder.encode(password))
                             .build());
             admin.setRole(Role.ADMIN);
             users.save(admin);
         };
     }
 }


