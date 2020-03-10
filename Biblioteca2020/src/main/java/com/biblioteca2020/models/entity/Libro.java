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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.biblioteca2020.models.entity.Categoria;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "libros")
@Data
public class Libro implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// LIBRO(*):LOCAL(1)
	// @JsonIgnore
	// @Size(min = 1)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "local_id")
	private Local local;

	// LIBRO(1):PRESTAMO(*)
	@OneToMany(mappedBy = "libro", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;
	
	// LIBRO(*):CATEGORIA(1)
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

	@NotBlank
	@Column(length = 100, nullable = false)
	@Size(min = 1, max = 100)
	private String titulo;

	@NotBlank
	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String autor;

	@Column(length = 255)
	@Size(max = 255)
	private String descripcion;

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

	@PrePersist
	public void prePersist() {
		fechaRegistro = new Date();
		estado = true;
	}

	private static final long serialVersionUID = 1L;
}
