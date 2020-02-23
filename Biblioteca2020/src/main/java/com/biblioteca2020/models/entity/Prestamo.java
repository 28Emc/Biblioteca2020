package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "prestamos")
public class Prestamo implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "libro_id", nullable = false)
	private Libro libro;

	@NotNull
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_despacho;

	private Boolean devolucion;

	@Column(length = 255, nullable = true)
	@Size(min = 1, max = 255)
	private String observaciones;

	@PrePersist
	public void prePersist() {
		fecha_despacho = new Date();
		devolucion = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Libro getLibro() {
		return libro;
	}

	public void setLibro(Libro libro) {
		this.libro = libro;
	}

	public Date getFecha_despacho() {
		return fecha_despacho;
	}

	public void setFecha_despacho(Date fecha_despacho) {
		this.fecha_despacho = fecha_despacho;
	}

	public Boolean getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(Boolean devolucion) {
		this.devolucion = devolucion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Prestamo(@NotNull Usuario usuario, @NotNull Libro libro, @NotNull Date fecha_despacho, Boolean devolucion,
			@Size(min = 1, max = 255) String observaciones) {
		super();
		this.usuario = usuario;
		this.libro = libro;
		this.fecha_despacho = fecha_despacho;
		this.devolucion = devolucion;
		this.observaciones = observaciones;
	}

	@Override
	public String toString() {
		return "Prestamo [id=" + id + ", usuario=" + usuario.getId() + ", libro=" + libro.getId() + ", fecha_despacho="
				+ fecha_despacho + ", devolucion=" + devolucion + ", observaciones=" + observaciones + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
