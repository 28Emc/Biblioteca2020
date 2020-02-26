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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.biblioteca2020.models.entity.Role;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Size(min=1)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "usuarios_roles",
		joinColumns=@JoinColumn(name="usuario_id"),
		inverseJoinColumns=@JoinColumn(name="rol_id"))
	private Set<Role> roles;

	@Column(length = 30)
	@NotBlank
	@Size(min = 3, max = 30)
	private String nombres;

	@Column(length = 60)
	@NotBlank
	@Size(min = 4, max = 60)
	private String apellidos;

	@Column(length = 8, name = "nro_documento")
	@NotBlank
	@Size(max = 8)
	private String nroDocumento;

	@Column(length = 200, nullable = true)
	@Size(max = 200)
	private String direccion;

	@Column(length = 30, nullable = true)
	@Size(max = 30)
	@Email
	private String email;

	@Column(length = 9)
	@Size(max = 9)
	private String celular;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_registro;

	@NotBlank
	@Column(length = 30)
	private String username;
	
	@NotBlank
	@Column(length = 60)
	private String password;
	
	@NotBlank
	@Transient
	private String passwordConfirmacion;

	private Boolean estado;

	// USER(1):PRESTAMOS(*)
	@OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public void roleSize() {
		roles.size();
	}

	public Usuario() {
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

	public String getNroDocumento() {
		return nroDocumento;
	}

	public void setNroDocumento(String nroDocumento) {
		this.nroDocumento = nroDocumento;
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

	public String getCelular() {
		return celular;
	}

	public void setCelular(String celular) {
		this.celular = celular;
	}

	public Date getFecha_registro() {
		return fecha_registro;
	}

	public void setFecha_registro(Date fecha_registro) {
		this.fecha_registro = fecha_registro;
	}

	public String getPasswordConfirmacion() {
		return passwordConfirmacion;
	}

	public void setPasswordConfirmacion(String passwordConfirmacion) {
		this.passwordConfirmacion = passwordConfirmacion;
	}

	public List<Prestamo> getPrestamos() {
		return prestamos;
	}

	public void setPrestamos(List<Prestamo> prestamos) {
		this.prestamos = prestamos;
	}

	public Usuario(Set<Role> roles, @NotBlank @Size(min = 3, max = 30) String nombres,
			@NotBlank @Size(min = 4, max = 60) String apellidos, @NotBlank @Size(max = 8) String nroDocumento,
			@Size(max = 200) String direccion, @Size(max = 30) @Email String email, @Size(max = 9) String celular,
			@NotBlank String username) {
		this.roles = roles;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.nroDocumento = nroDocumento;
		this.direccion = direccion;
		this.email = email;
		this.celular = celular;
		this.username = username;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}