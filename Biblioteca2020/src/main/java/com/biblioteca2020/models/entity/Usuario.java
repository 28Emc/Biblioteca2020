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
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// USER(*):ROLE(*)
	@Size(min = 1)
	// CAMBIÈ AQUI DE LAZY A EAGER PARA MANEJAR METODOS EN EL LOGOUTSUCCESSHANDLER
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
	private Set<Role> roles;

	// USER(1):PRESTAMOS(*)
	@JsonIgnore
	@OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;

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
	@Pattern(regexp = "^\\d{8}$")
	private String nroDocumento;

	@Column(length = 200, nullable = true)
	@Size(max = 200)
	private String direccion;

	@Column(length = 30, unique = true)
	@Size(max = 30)
	@Email
	@NotBlank
	private String email;

	@Pattern(regexp = "^\\d{9}$")
	@Column(length = 9, nullable = true, unique = true)
	private String celular;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fecha_registro;

	@NotBlank
	@Column(length = 30, unique = true)
	private String username;

	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,}$")
	@NotBlank
	@Column(length = 60)
	private String password;

	@Transient
	@Column(nullable = true)
	private String passwordConfirmacion;

	@Column(nullable = false)
	private Boolean estado;
	
	private String foto_usuario;

	@PrePersist
	public void prePersist() {
		fecha_registro = new Date();
		//estado = false;
	}

	private static final long serialVersionUID = 1L;

}