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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestamos")
@Getter
@Setter
@RequiredArgsConstructor
public class Prestamo implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// PRESTAMOS(*):USER(1)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	@Valid
	private Usuario usuario;

	// PRESTAMOS(*):EMPLEADO(1)	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "empleado_id", nullable = false)
	//@Valid
	private Empleado empleado;

	// PRESTAMOS(*):LIBRO(1)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "libro_id", nullable = false)
	@Valid
	private Libro libro;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_despacho;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fecha_devolucion;

	private Boolean devolucion;

	@Column(length = 255)
	@Size(min = 1, max = 255)
	private String observaciones;

	private static final long serialVersionUID = 1L;
}
