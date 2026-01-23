package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.dto.AuthRequest;
import com.mikelekan.artgallery.dto.AuthResponse;
import com.mikelekan.artgallery.model.User;
import com.mikelekan.artgallery.repository.UserRepository;
import com.mikelekan.artgallery.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        // 1. Authenticate the user
        // This line tells Spring: "Go check the DB and compare hashed passwords"
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
        );

        // 2. If successful, load the user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUserName());

        // 3. Generate the "Golden Ticket" (JWT)
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Return the token to the React frontend
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        if (userRepository.findByUserName(authRequest.getUserName()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Create new user with ENCRYPTED password
        User newUser = User.builder()
                .userName(authRequest.getUserName())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .build();

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Just a placeholder endpoint
        // Real logout happens on frontend
        return ResponseEntity.ok("Logged out successfully");
    }
}