package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "empresas")
@Data
public class Empresa implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// EMPRESA(1):LOCAL(*)
	// @JsonIgnore
	@OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Local> locales;

	@NotBlank
	@Column(length = 100, name = "razon_social", unique = true)
	private String razonSocial;

	@NotBlank
	@Column(length = 11, unique = true)
	@Pattern(regexp = "^\\d{11}$")
	private String ruc;

	@NotBlank
	@Column(length = 100, nullable = true)
	private String direccion;

	@Column(nullable = false)
	private Boolean estado;

	@PrePersist
	public void prePersist() {
		estado = true;
	}

	private static final long serialVersionUID = 1L;
}
