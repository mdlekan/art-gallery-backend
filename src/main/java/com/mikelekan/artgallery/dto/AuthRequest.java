package com.mikelekan.artgallery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuthRequest
{
	@NotBlank(message = "Username cannot be empty")
	private String userName;

	@Size(min = 6, message = "Password must be at least 6 characters")
	private String password;
}
