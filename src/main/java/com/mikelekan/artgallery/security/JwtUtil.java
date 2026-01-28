package com.mikelekan.artgallery.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT Utility Class - Handles all JWT token operations
 *
 * <p>
 * What does this class do? 1. Generate JWT tokens (when user logs in) 2.
 * Validate JWT tokens (on every protected request) 3. Extract information from
 * tokens (username, expiration, etc.)
 */
@Component
public class JwtUtil
{

	/**
	 * Secret key for signing tokens This should be in application.properties:
	 * jwt.secret=your-256-bit-secret-key-here
	 *
	 * <p>
	 * Why 256-bit? - HS256 algorithm requires at least 256 bits (32 characters) -
	 * Longer = more secure - Must be kept SECRET - if someone gets this, they can
	 * forge tokens!
	 */
	@Value("${jwt.secret}")
	private String SECRET_KEY;

	/**
	 * Token expiration time: 24 hours (in milliseconds) 1000ms * 60s * 60m * 24h =
	 * 86,400,000ms
	 *
	 * <p>
	 * After 24 hours, the token expires and user must login again You can adjust
	 * this: - 1 hour: 1000 * 60 * 60 - 7 days: 1000 * 60 * 60 * 24 * 7
	 */
	private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

	/**
	 * Extract username from token
	 *
	 * <p>
	 * How it works: 1. Parse the token 2. Get the "subject" claim (we store
	 * username here) 3. Return it
	 */
	public String extractUsername(String token)
	{
		return extractClaim(token, Claims::getSubject);
	}

	/** Extract expiration date from token Used to check if token is still valid */
	public Date extractExpiration(String token)
	{
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Generic method to extract any claim from token
	 *
	 * <p>
	 * What's a claim? - A piece of information stored in the JWT - Examples:
	 * username (subject), expiration, custom data
	 *
	 * @param claimsResolver
	 *            Function that extracts specific claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
	{
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Parse token and extract all claims
	 *
	 * <p>
	 * This is where token validation happens: - Verifies the signature (using
	 * SECRET_KEY) - If signature is invalid, throws exception - If token is
	 * expired, throws exception
	 */
	private Claims extractAllClaims(String token)
	{
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * Convert SECRET_KEY string to SecretKey object jjwt library needs this format
	 * for signing/verifying
	 */
	private SecretKey getSigningKey()
	{
		byte[] keyBytes = SECRET_KEY.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Check if token is expired Compares token's expiration date with current date
	 */
	private Boolean isTokenExpired(String token)
	{
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Generate token for a user
	 *
	 * <p>
	 * What goes in the token? - Subject: username - Issued at: current time -
	 * Expiration: current time + 24 hours - Signature: signed with SECRET_KEY
	 *
	 * @param userDetails
	 *            The user to generate token for
	 * @return JWT token string
	 */
	public String generateToken(UserDetails userDetails)
	{
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Create the actual JWT token
	 *
	 * <p>
	 * Token structure: header.payload.signature
	 *
	 * <p>
	 * Header: {"alg": "HS256", "typ": "JWT"} Payload: {"sub": "username", "iat":
	 * 1234567890, "exp": 1234654290} Signature: HMACSHA256(base64(header) + "." +
	 * base64(payload), SECRET_KEY)
	 */
	private String createToken(Map<String, Object> claims, String subject)
	{
		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).signWith(getSigningKey()).compact();
	}

	/**
	 * Validate token
	 *
	 * <p>
	 * Checks: 1. Username in token matches the UserDetails 2. Token is not expired
	 *
	 * @param token
	 *            JWT token to validate
	 * @param userDetails
	 *            User to validate against
	 * @return true if valid, false otherwise
	 */
	public Boolean validateToken(String token, UserDetails userDetails)
	{
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}
