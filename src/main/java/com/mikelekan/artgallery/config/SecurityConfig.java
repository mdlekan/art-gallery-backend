package com.mikelekan.artgallery.config;

import static org.springframework.http.HttpMethod.POST;

import com.mikelekan.artgallery.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		http
				.cors(cors -> cors.configure(http))
				// 1. Disable CSRF for webhooks (Required for Stripe)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/webhooks/**"))

				.authorizeHttpRequests(auth -> auth
						//Public Endpoints (No Token Required)
						.requestMatchers("/api/auth/**", "/error").permitAll()
						.requestMatchers("/api/webhooks/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/artworks/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/orders", "/api/customers").permitAll()

						//Catch-all
						.anyRequest().authenticated()
				)

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception
	{
		AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

		authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

		return authManagerBuilder.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
}
