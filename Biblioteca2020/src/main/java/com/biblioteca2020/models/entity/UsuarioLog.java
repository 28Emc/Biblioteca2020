package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "usuarios_log")
@Data
public class UsuarioLog implements Serializable {

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

	private String foto_usuario;

	private String new_foto_usuario;

	private String tipo_operacion;

	private Date insert_at;

	private Date update_at;

	private Date delete_at;

	public UsuarioLog(Long role_id, String nombres, String new_nombres, String apellidos, String new_apellidos,
			String nroDocumento, String new_nroDocumento, String direccion, String new_direccion, String email,
			String new_email, String celular, String new_celular, Date fecha_registro, Date new_fecha_registro,
			String username, String new_username, String password, String new_password, Boolean estado,
			Boolean new_estado, String foto_usuario, String new_foto_usuario, String tipo_operacion, Date insert_at,
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
		this.foto_usuario = foto_usuario;
		this.new_foto_usuario = new_foto_usuario;
		this.tipo_operacion = tipo_operacion;
		this.insert_at = insert_at;
		this.update_at = update_at;
		this.delete_at = delete_at;
	}

	private static final long serialVersionUID = 1L;
}