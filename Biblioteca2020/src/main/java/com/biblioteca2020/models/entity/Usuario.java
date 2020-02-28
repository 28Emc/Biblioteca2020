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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.biblioteca2020.models.entity.Role;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// USER(*):ROLE(*)
	@Size(min = 1)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
	private Set<Role> roles;

	@Column(length = 30)
	@NotBlank
	@Size(min = 3, max = 30)
	private String nombres;

	@Column(length = 60)
	@NotBlank
	@Size(min = 4, max = 60)
	private String apellidos;

	@Column(length = 8, name = "nro_documento", unique = true)
	@NotBlank
	//@Size(min = 8, max = 8)
	@Pattern(regexp = "^\\d{8}$")
	private String nroDocumento;

	@Column(length = 200, nullable = true)
	@Size(max = 200)
	private String direccion;

	@Column(length = 30, nullable = true, unique = true)
	@Size(max = 30)
	@Email
	private String email;

	@Column(length = 9, nullable = true, unique = true)
	@Size(max = 9)
	//@Pattern(regexp = "^\\d{9}+$")
	private String celular;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_registro;

	@NotBlank
	@Column(length = 30, unique = true)
	private String username;

	@NotBlank
	@Column(length = 60)
	private String password;

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

	public Usuario() {
	}

	public Usuario(@Size(min = 1) Set<Role> roles, @NotBlank @Size(min = 3, max = 30) String nombres,
			@NotBlank @Size(min = 4, max = 60) String apellidos, @NotBlank @Size(min = 8, max = 8) String nroDocumento,
			@Size(max = 200) String direccion, @Size(max = 30) @Email String email, @Size(max = 9) String celular,
			Date fecha_registro, @NotBlank String username, @NotBlank String password, String passwordConfirmacion,
			Boolean estado, List<Prestamo> prestamos) {
		this.roles = roles;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.nroDocumento = nroDocumento;
		this.direccion = direccion;
		this.email = email;
		this.celular = celular;
		this.fecha_registro = fecha_registro;
		this.username = username;
		this.password = password;
		this.passwordConfirmacion = passwordConfirmacion;
		this.estado = estado;
		this.prestamos = prestamos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apellidos == null) ? 0 : apellidos.hashCode());
		result = prime * result + ((celular == null) ? 0 : celular.hashCode());
		result = prime * result + ((direccion == null) ? 0 : direccion.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((estado == null) ? 0 : estado.hashCode());
		result = prime * result + ((fecha_registro == null) ? 0 : fecha_registro.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nombres == null) ? 0 : nombres.hashCode());
		result = prime * result + ((nroDocumento == null) ? 0 : nroDocumento.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((passwordConfirmacion == null) ? 0 : passwordConfirmacion.hashCode());
		result = prime * result + ((prestamos == null) ? 0 : prestamos.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		if (apellidos == null) {
			if (other.apellidos != null)
				return false;
		} else if (!apellidos.equals(other.apellidos))
			return false;
		if (celular == null) {
			if (other.celular != null)
				return false;
		} else if (!celular.equals(other.celular))
			return false;
		if (direccion == null) {
			if (other.direccion != null)
				return false;
		} else if (!direccion.equals(other.direccion))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (fecha_registro == null) {
			if (other.fecha_registro != null)
				return false;
		} else if (!fecha_registro.equals(other.fecha_registro))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nombres == null) {
			if (other.nombres != null)
				return false;
		} else if (!nombres.equals(other.nombres))
			return false;
		if (nroDocumento == null) {
			if (other.nroDocumento != null)
				return false;
		} else if (!nroDocumento.equals(other.nroDocumento))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (passwordConfirmacion == null) {
			if (other.passwordConfirmacion != null)
				return false;
		} else if (!passwordConfirmacion.equals(other.passwordConfirmacion))
			return false;
		if (prestamos == null) {
			if (other.prestamos != null)
				return false;
		} else if (!prestamos.equals(other.prestamos))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}