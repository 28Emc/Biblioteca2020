package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "libros")
public class Libro implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String titulo;

	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String autor;

	@Column(length = 255, nullable = true)
	@Size(min = 1, max = 255)
	private String descripcion;

	@Column(length = 50)
	@Size(min = 1, max = 50)
	private String categoria;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_publicacion;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fechaRegistro;

	private Boolean estado;

	@PrePersist
	public void prePersist() {
		fechaRegistro = new Date();
		estado = true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public Date getFecha_publicacion() {
		return fecha_publicacion;
	}

	public void setFecha_publicacion(Date fecha_publicacion) {
		this.fecha_publicacion = fecha_publicacion;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Libro(@Size(min = 1, max = 100) String titulo, @Size(min = 1, max = 100) String autor,
			@Size(min = 1, max = 255) String descripcion, @Size(min = 1, max = 50) String categoria,
			Date fecha_publicacion, Date fechaRegistro, Boolean estado) {
		super();
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.categoria = categoria;
		this.fecha_publicacion = fecha_publicacion;
		this.fechaRegistro = fechaRegistro;
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Libro [id=" + id + ", titulo=" + titulo + ", autor=" + autor + ", descripcion=" + descripcion
				+ ", categoria=" + categoria + ", fecha_publicacion=" + fecha_publicacion + ", fechaRegistro="
				+ fechaRegistro + ", estado=" + estado + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
