package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Entity
@Table(name = "empleados_log")
@Data
public class EmpleadoLog implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long role_id;

	private String nombres;

	private String new_nombres;

	private String apellidos;

	private String new_apellidos;

	private String nroDocumento;

	private String new_nroDocumento;

	private String direccion;

	private String new_direccion;

	private String email;

	private String new_email;

	private String celular;

	private String new_celular;

	private Date fecha_registro;

	private Date new_fecha_registro;

	private String username;

	private String new_username;

	private String password;

	private String new_password;

	private Boolean estado;

	private Boolean new_estado;

	private String foto_empleado;

	private String new_foto_empleado;

	private String tipo_operacion;

	private Date insert_at;

	private Date update_at;

	private Date delete_at;

	private static final long serialVersionUID = 1L;

	public EmpleadoLog(Long role_id, String nombres, String new_nombres, String apellidos, String new_apellidos,
			String nroDocumento, String new_nroDocumento, String direccion, String new_direccion, String email,
			String new_email, String celular, String new_celular, Date fecha_registro, Date new_fecha_registro,
			String username, String new_username, String password, String new_password, Boolean estado,
			Boolean new_estado, String foto_empleado, String new_foto_empleado, String tipo_operacion, Date insert_at,
			Date update_at, Date delete_at) {
		this.role_id = role_id;
		this.nombres = nombres;
		this.new_nombres = new_nombres;
		this.apellidos = apellidos;
		this.new_apellidos = new_apellidos;
		this.nroDocumento = nroDocumento;
		this.new_nroDocumento = new_nroDocumento;
		this.direccion = direccion;
		this.new_direccion = new_direccion;
		this.email = email;
		this.new_email = new_email;
		this.celular = celular;
		this.new_celular = new_celular;
		this.fecha_registro = fecha_registro;
		this.new_fecha_registro = new_fecha_registro;
		this.username = username;
		this.new_username = new_username;
		this.password = password;
		this.new_password = new_password;
		this.estado = estado;
		this.new_estado = new_estado;
		this.foto_empleado = foto_empleado;
		this.new_foto_empleado = new_foto_empleado;
		this.tipo_operacion = tipo_operacion;
		this.insert_at = insert_at;
		this.update_at = update_at;
		this.delete_at = delete_at;
	}
}