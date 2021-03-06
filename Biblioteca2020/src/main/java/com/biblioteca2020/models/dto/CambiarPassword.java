package com.biblioteca2020.models.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class CambiarPassword {

	@NotNull
	private Long id;

	@NotBlank
	private String passwordActual;

	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,}$")
	@NotBlank
	private String nuevaPassword;

	@NotBlank
	private String confirmarPassword;
	
	public CambiarPassword() {		
	}
	
	public CambiarPassword(Long id) {
		this.id = id;
	}
}
