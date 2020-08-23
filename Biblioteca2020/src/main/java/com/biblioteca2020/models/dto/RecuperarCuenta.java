package com.biblioteca2020.models.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecuperarCuenta {

	@NotBlank
	private String nroDocumento;

	@NotBlank
	private String email;
}
