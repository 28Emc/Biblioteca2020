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
@Table(name = "empleados")
@Data
public class Empleado implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// EMPLEADO(*):ROLE(2) - EL EMPLEADO PUEDE SER EMPLEADO Y/O SUPERVISOR
	@Size(min = 1)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "empleados_roles", joinColumns = @JoinColumn(name = "empleado_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
	private Set<Role> roles;

	// EMPLEADO(1):PRESTAMOS(*)
	@JsonIgnore
	@OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private Local local;

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

	@Column(length = 30, nullable = true, unique = true)
	@Size(max = 30)
	@Email
	@NotBlank
	private String email;

	@Column(length = 9, nullable = true, unique = true)
	@Pattern(regexp = "^\\d{9}$")
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
	/* Patron de contraseña:
	^                 # inicio cadena
	(?=.*[0-9])       # mínimo 1 dígito
	(?=.*[a-z])       # mínimo 1 letra minúscula
	(?=.*[A-Z])       # mínimo 1 letra mayúscula
	(?=\S+$)          # sin espacios
	.{5,}             # mínimo 5 caracteres
	$                 # fin cadena
	*/
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,}$")
	private String password;

	@Transient
	@Column(nullable = true)
	private String passwordConfirmacion;

	@Column(nullable = false)
	private Boolean estado;

	private String foto_empleado;

	@PrePersist
	public void prePersist() {
		fecha_registro = new Date();
		estado = true;
		foto_empleado = "no-image.jpg";
	}

	private static final long serialVersionUID = 1L;
}