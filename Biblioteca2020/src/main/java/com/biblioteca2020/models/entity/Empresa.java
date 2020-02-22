package com.biblioteca2020.models.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
@Table(name = "empresas")
public class Empresa implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotEmpty
	@Column(length = 100, unique = true)
	@Size(min = 1, max = 100)
	private String razon_social;

	@NotEmpty
	@Column(length = 11, unique = true)
	@Size(min = 1, max = 11)
	private String ruc;

	@Column(length = 100, nullable = true)
	@Size(min = 1, max = 100)
	private String direccion;

	private Boolean estado;

	@PrePersist
	public void prePersist() {
		estado = true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRazon_social() {
		return razon_social;
	}

	public void setRazon_social(String razon_social) {
		this.razon_social = razon_social;
	}

	public String getRuc() {
		return ruc;
	}

	public void setRuc(String ruc) {
		this.ruc = ruc;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Empresa(@Size(min = 1, max = 100) String razon_social, @Size(min = 1, max = 11) String ruc,
			@Size(min = 1, max = 100) String direccion, Boolean estado) {
		super();
		this.razon_social = razon_social;
		this.ruc = ruc;
		this.direccion = direccion;
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Empresa [id=" + id + ", razon_social=" + razon_social + ", ruc=" + ruc + ", direccion=" + direccion
				+ ", estado=" + estado + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
