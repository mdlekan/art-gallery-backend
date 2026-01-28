package com.mikelekan.artgallery.security;

import com.mikelekan.artgallery.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation
 *
 * <p>
 * This is the bridge between Spring Security and our User database. Spring
 * Security calls this to load users during authentication.
 *
 * <p>
 * Why create this? - Spring Security doesn't know about OUR User entity -
 * Spring Security doesn't know about OUR UserRepository - We need to teach it
 * how to load users from OUR database
 */
@Service
public class CustomerUserDetailsService implements UserDetailsService
{
	private final UserRepository userRepository;

	public CustomerUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Load user by username
	 *
	 * <p>
	 * This is THE method Spring Security calls to get user info.
	 *
	 * <p>
	 * Called in two scenarios: 1. During login - to verify password 2. In JWT
	 * filter - to load user for authentication
	 *
	 * @param userName
	 *            Username to look up
	 * @return UserDetails (our User entity implements this)
	 * @throws UsernameNotFoundException
	 *             if user not found
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException
	{
		return userRepository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));
	}
}
