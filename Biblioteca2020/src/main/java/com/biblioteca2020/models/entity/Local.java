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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.biblioteca2020.models.entity.Empresa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locales")
@Getter
@Setter
@RequiredArgsConstructor
public class Local implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// @JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;
	
	// LOCAL(1):EMPLEADO(*)
	@OneToMany(mappedBy = "local", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Empleado> empleados;

	@NotBlank
	@Column(length = 100, unique = true, nullable = false)
	@Size(min = 10, max = 100)
	private String direccion;

	@Column(length = 255)
	@Size(max = 255)
	private String observaciones;

	@Column(nullable = false)
	private Boolean estado;

	@PrePersist
	public void prePersist() {
		estado = true;
	}

	// EN ESTA CLASE EL MÉTODO HASHCODE NO FUNCIONA Y CAUSA UN ERROR DE
	// STACKOVERFLOW

	private static final long serialVersionUID = 1L;
}
