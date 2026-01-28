package com.mikelekan.artgallery.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	private final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
			HandlerExceptionResolver handlerExceptionResolver) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException
	{
		String requestPath = request.getRequestURI();

		// 1. If the user is trying to login or register, DON'T check for a JWT
		if (requestPath.startsWith("/api/auth/"))
		{
			filterChain.doFilter(request, response);
			return;
		}
		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer "))
		{
			filterChain.doFilter(request, response);
			return;
		}

		try
		{
			final String jwt = authHeader.substring(7);

			// Ensure the string isn't empty/whitespace before calling JwtUtil
			if (jwt.isBlank())
			{
				filterChain.doFilter(request, response);
				return;
			}

			final String username = jwtUtil.extractUsername(jwt);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
			{
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtUtil.validateToken(jwt, userDetails))
				{
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}

		} catch (Exception e)
		{
			handlerExceptionResolver.resolveException(request, response, null, e);
			logger.error("Cannot set user authentication: {}", e);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
