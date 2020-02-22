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
	@Column(nullable = false)
	// private Usuario usuario;
	private Long usuario_id;

	@NotNull
	@Column(nullable = false)
	// private Libro libro;
	private Long libro_id;

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

	public Long getUsuario_id() {
		return usuario_id;
	}

	public void setUsuario_id(Long usuario_id) {
		this.usuario_id = usuario_id;
	}

	public Long getLibro_id() {
		return libro_id;
	}

	public void setLibro_id(Long libro_id) {
		this.libro_id = libro_id;
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

	public Prestamo(Long usuario_id, Long libro_id, Date fecha_despacho, Boolean devolucion,
			@Size(min = 1, max = 255) String observaciones) {
		super();
		this.usuario_id = usuario_id;
		this.libro_id = libro_id;
		this.fecha_despacho = fecha_despacho;
		this.devolucion = devolucion;
		this.observaciones = observaciones;
	}

	@Override
	public String toString() {
		return "Prestamo [id=" + id + ", usuario_id=" + usuario_id + ", libro_id=" + libro_id + ", fecha_despacho="
				+ fecha_despacho + ", devolucion=" + devolucion + ", observaciones=" + observaciones + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
