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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.biblioteca2020.models.entity.Empresa;

@Entity
@Table(name = "locales")
public class Local implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToMany
	@JoinTable(name = "locales_libros", joinColumns = @JoinColumn(name = "local_id"), inverseJoinColumns = @JoinColumn(name = "libro_id"))
	private Set<Libro> libros;
	
	@NotNull
	@JoinColumn(name = "empresa_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Empresa empresa;

	@NotEmpty
	@Column(length = 100)
	@Size(min = 10, max = 100)
	private String direccion;

	@Column(length = 255, nullable = true)
	@Size(min = 1, max = 255)
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
		return "Local [id=" + id + ", libros=" + libros + ", empresa=" + empresa + ", direccion=" + direccion
				+ ", observaciones=" + observaciones + ", estado=" + estado + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
