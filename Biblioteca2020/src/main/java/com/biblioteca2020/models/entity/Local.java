package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.biblioteca2020.models.entity.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "locales")
public class Local implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// LOGICA DE DETALLE-LOCAL (DESPUES DE VER-LOCALES)
	// -EN EL FORM SELECCIONO EL NOMBRE DEL LIBRO (CAPTURO ID),
	// EN EL CONTROLLER BUSCO EL LIBRO POR ESE ID,
	// BUSCO EL LOCAL SEGUN EL ID DE ESE MISMO LOCAL,
	// LOCAL.ADDLIBROS(['AQUI VA EL LIBRO ENCONTRADO AL INICIO'])
	// GUARDO CON SAVE() EL LOCAL

	// LOCAL(*):LIBRO(*)
	@ManyToMany
	//@JsonIgnore
	@JoinTable(name = "locales_libros", joinColumns = @JoinColumn(name = "local_id"), inverseJoinColumns = @JoinColumn(name = "libro_id"))
	private Set<Libro> libros;
	
	//@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@NotBlank
	@Column(length = 100, unique = true)
	@Size(min = 10, max = 100)
	private String direccion;

	@Column(length = 255, nullable = true)
	@Size(max = 255)
	private String observaciones;

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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Set<Libro> getLibros() {
		return libros;
	}

	public void setLibros(Set<Libro> libros) {
		this.libros = libros;
	}

	public Local() {
	}

	public Local(Set<Libro> libros, Empresa empresa, @Size(min = 10, max = 100) String direccion,
			@Size(min = 1, max = 255) String observaciones, Boolean estado) {
		super();
		this.libros = libros;
		this.empresa = empresa;
		this.direccion = direccion;
		this.observaciones = observaciones;
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Local [id=" + id + ", libros=" + libros + ", empresa=" + empresa.getId() + ", direccion=" + direccion
				+ ", observaciones=" + observaciones + ", estado=" + estado + "]";
	}
	
	// EN ESTA CLASE EL MÉTODO HASHCODE NO FUNCIONA Y CAUSA UN ERROR DE STACKOVERFLOW

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Local other = (Local) obj;
		if (direccion == null) {
			if (other.direccion != null)
				return false;
		} else if (!direccion.equals(other.direccion))
			return false;
		if (empresa == null) {
			if (other.empresa != null)
				return false;
		} else if (!empresa.equals(other.empresa))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (libros == null) {
			if (other.libros != null)
				return false;
		} else if (!libros.equals(other.libros))
			return false;
		if (observaciones == null) {
			if (other.observaciones != null)
				return false;
		} else if (!observaciones.equals(other.observaciones))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
