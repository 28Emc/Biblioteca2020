package com.biblioteca2020.models.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "libros")
@Getter
@Setter
@RequiredArgsConstructor
public class Libro implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// LIBRO(*):LOCAL(1)
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "local_id")
	private Local local;

	// LIBRO(1):PRESTAMO(*)
	@JsonIgnore
	@OneToMany(mappedBy = "libro", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;

	// LIBRO(*):CATEGORIA(1)
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoria_id")
	// ANOTACIÓN QUE SIRVE PARA VALIDAR CAMPOS DE ESTA ENTIDAD EN OTROS FORMULARIOS
	// (EJM. LIBRO)
	@Valid
	private Categoria categoria;

	@NotBlank
	@Column(length = 100, nullable = false)
	@Size(min = 1, max = 100)
	private String titulo;

	@NotBlank
	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String autor;

	@Type(type = "text")
	private String descripcion;

	@Transient
	private String descripcionMin;

	// yyyy-mm-dd
	@Column(name = "fecha_publicacion", nullable = false)
	@Pattern(regexp = "^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$")
	private String fechaPublicacion;

	@Column(name = "fecha_registro", nullable = false)
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fechaRegistro;

	@Column(nullable = false)
	private Boolean estado;

	@Column(length = 4)
	@Min(value = 1)
	@Max(value = 9999)
	@NotNull
	private Integer stock;

	private String foto_libro;

	@PrePersist
	public void prePersist() {
		fechaRegistro = new Date();
		estado = true;
		foto_libro = "no-book.jpg";
	}

	private static final long serialVersionUID = 1L;
}
