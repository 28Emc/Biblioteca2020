package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "personas")
public class Persona implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@OneToOne
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(length = 30, nullable = true)
	@Size(min = 3, max = 30)
	private String nombres;

	@Column(length = 60, nullable = true)
	@Size(min = 4, max = 60)
	private String apellidos;

	@Column(length = 8, unique = true, nullable = true)
	@Size(min = 1, max = 8)
	private Integer nro_documento;

	@Column(length = 200, nullable = true)
	@Size(min = 10, max = 200)
	private String direccion;

	@Column(length = 30, nullable = true)
	@Size(min = 10, max = 30)
	@Email
	private String email;

	@Column(length = 9, nullable = true)
	@Size(min = 1, max = 9)
	private Integer celular;

	@Column(name = "fecha_registro", nullable = true)
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_registro;

	private Boolean estado;

	@PrePersist
	public void prePersist() {
		fecha_registro = new Date();
		estado = true;
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

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public Integer getNro_documento() {
		return nro_documento;
	}

	public void setNro_documento(Integer nro_documento) {
		this.nro_documento = nro_documento;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getCelular() {
		return celular;
	}

	public void setCelular(Integer celular) {
		this.celular = celular;
	}

	public Date getFecha_registro() {
		return fecha_registro;
	}

	public void setFecha_registro(Date fecha_registro) {
		this.fecha_registro = fecha_registro;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Persona(Usuario usuario, @Size(min = 3, max = 30) String nombres, @Size(min = 4, max = 60) String apellidos,
			@Size(min = 1, max = 8) Integer nro_documento, @Size(min = 10, max = 200) String direccion,
			@Size(min = 10, max = 30) @Email String email, @Size(min = 1, max = 9) Integer celular, Date fecha_registro,
			Boolean estado) {
		super();
		this.usuario = usuario;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.nro_documento = nro_documento;
		this.direccion = direccion;
		this.email = email;
		this.celular = celular;
		this.fecha_registro = fecha_registro;
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Persona [id=" + id + ", usuario=" + usuario + ", nombres=" + nombres + ", apellidos=" + apellidos
				+ ", nro_documento=" + nro_documento + ", direccion=" + direccion + ", email=" + email + ", celular="
				+ celular + ", fecha_registro=" + fecha_registro + ", estado=" + estado + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
