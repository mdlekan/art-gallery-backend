package com.mikelekan.artgallery.exception;

import com.mikelekan.artgallery.dto.ErrorResponse;
import io.jsonwebtoken.MalformedJwtException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{

	// Catch specific JWT errors
	@ExceptionHandler(MalformedJwtException.class)
	public ResponseEntity<ErrorResponse> handleMalformedJwt(MalformedJwtException ex)
	{
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
				"Invalid token format. Please log in again.", System.currentTimeMillis(), null);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// Catch generic "User Not Found" or other security issues
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex)
	{
		ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred: " + ex.getMessage(), System.currentTimeMillis(), null);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex)
	{
		Map<String, String> fieldErrors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed",
				System.currentTimeMillis(), null);

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BusinessLogicException.class)
	public ResponseEntity<ErrorResponse> handleBusinessLogic(BusinessLogicException ex)
	{
		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), // 409 is great for "already exists" or
																				// "already sold"
				ex.getMessage(), System.currentTimeMillis(),
				// No specific field errors needed here
				null);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex)
	{
		String message = "Database error: A record with this information already exists.";

		// Optional: Parse the error message to find which field failed
		if (ex.getMessage() != null && ex.getMessage().contains("uc_user_name"))
		{
			message = "That username is already taken. Please choose another.";
		}

		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message, System.currentTimeMillis(), null);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	// Add this specifically for login failures
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex)
	{
		ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), // 401
				"Invalid username or password.", System.currentTimeMillis(), null);
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}
}
