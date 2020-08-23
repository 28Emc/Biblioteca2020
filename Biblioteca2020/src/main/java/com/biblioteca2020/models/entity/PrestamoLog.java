package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestamos_log")
@Getter
@Setter
@RequiredArgsConstructor
public class PrestamoLog implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long prestamo_id;

	private Long empleado_id;

	private Long new_empleado_id;

	private Long libro_id;

	private Long new_libro_id;

	private Long usuario_id;

	private Long new_usuario_id;

	private String tipo_operacion;

	private String usuario;

	@Temporal(TemporalType.TIMESTAMP)
	private Date fecha_despacho;

	@Temporal(TemporalType.TIMESTAMP)
	private Date new_fecha_despacho;

	@Temporal(TemporalType.TIMESTAMP)
	private Date fecha_devolucion;

	@Temporal(TemporalType.TIMESTAMP)
	private Date new_fecha_devolucion;

	private Boolean devolucion;

	private Boolean new_devolucion;

	private String observaciones;

	private String new_observaciones;

	private Date insert_at;

	private Date update_at;

	private Date delete_at;

	private static final long serialVersionUID = 1L;

	public PrestamoLog(Long prestamo_id, Long empleado_id, Long new_empleado_id, Long libro_id, Long new_libro_id,
			Long usuario_id, Long new_usuario_id, String tipo_operacion, String usuario, Date fecha_despacho,
			Date new_fecha_despacho, Date fecha_devolucion, Date new_fecha_devolucion, Boolean devolucion,
			Boolean new_devolucion, String observaciones, String new_observaciones, Date insert_at, Date update_at,
			Date delete_at) {
		this.prestamo_id = prestamo_id;
		this.empleado_id = empleado_id;
		this.new_empleado_id = new_empleado_id;
		this.libro_id = libro_id;
		this.new_libro_id = new_libro_id;
		this.usuario_id = usuario_id;
		this.new_usuario_id = new_usuario_id;
		this.tipo_operacion = tipo_operacion;
		this.usuario = usuario;
		this.fecha_despacho = fecha_despacho;
		this.new_fecha_despacho = new_fecha_despacho;
		this.fecha_devolucion = fecha_devolucion;
		this.new_fecha_devolucion = new_fecha_devolucion;
		this.devolucion = devolucion;
		this.new_devolucion = new_devolucion;
		this.observaciones = observaciones;
		this.new_observaciones = new_observaciones;
		this.insert_at = insert_at;
		this.update_at = update_at;
		this.delete_at = delete_at;
	}
}
