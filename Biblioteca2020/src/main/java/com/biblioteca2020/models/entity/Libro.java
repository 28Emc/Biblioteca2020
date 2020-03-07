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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.biblioteca2020.models.entity.Categoria;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "libros")
public class Libro implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// LIBRO(*):LOCAL(*)
	// SIMILAR A USUARIO:ROLES
	//@JsonIgnore
	@Size(min = 1)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "locales_libros", joinColumns = @JoinColumn(name = "libro_id"), inverseJoinColumns = @JoinColumn(name = "local_id"))
	private Set<Local> locales;

	@NotEmpty
	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String titulo;

	@NotEmpty
	@Column(length = 100)
	@Size(min = 1, max = 100)
	private String autor;

	@Column(length = 255, nullable = true)
	@Size(min = 1, max = 255)
	private String descripcion;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;
	
	@NotBlank
	// yyyy-mm-dd
	@Pattern(regexp = "^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$")
	private String fecha_publicacion;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date fechaRegistro;

	private Boolean estado;

	// LIBRO(1):PRESTAMO(*)
	//@JsonIgnoreProperties(value = { "libro" })
	@OneToMany(mappedBy = "libro", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Prestamo> prestamos;

	@PrePersist
	public void prePersist() {
		fechaRegistro = new Date();
		estado = true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public String getFecha_publicacion() {
		return fecha_publicacion;
	}

	public void setFecha_publicacion(String fecha_publicacion) {
		this.fecha_publicacion = fecha_publicacion;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Set<Local> getLocales() {
		return locales;
	}

	public void setLocales(Set<Local> locales) {
		this.locales = locales;
	}

	public List<Prestamo> getPrestamos() {
		return prestamos;
	}

	public void setPrestamos(List<Prestamo> prestamos) {
		this.prestamos = prestamos;
	}

	public Libro(Set<Local> locales, @NotEmpty @Size(min = 1, max = 100) String titulo,
			@NotEmpty @Size(min = 1, max = 100) String autor, @Size(min = 1, max = 255) String descripcion,
			@NotEmpty @Size(min = 1, max = 50) Categoria categoria, @NotBlank String fecha_publicacion, Date fechaRegistro,
			Boolean estado, List<Prestamo> prestamos) {
		this.locales = locales;
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.categoria = categoria;
		this.fecha_publicacion = fecha_publicacion;
		this.fechaRegistro = fechaRegistro;
		this.estado = estado;
		this.prestamos = prestamos;
	}

	public Libro() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((autor == null) ? 0 : autor.hashCode());
		result = prime * result + ((categoria == null) ? 0 : categoria.hashCode());
		result = prime * result + ((descripcion == null) ? 0 : descripcion.hashCode());
		result = prime * result + ((estado == null) ? 0 : estado.hashCode());
		result = prime * result + ((fechaRegistro == null) ? 0 : fechaRegistro.hashCode());
		result = prime * result + ((fecha_publicacion == null) ? 0 : fecha_publicacion.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((locales == null) ? 0 : locales.hashCode());
		result = prime * result + ((prestamos == null) ? 0 : prestamos.hashCode());
		result = prime * result + ((titulo == null) ? 0 : titulo.hashCode());
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
		Libro other = (Libro) obj;
		if (autor == null) {
			if (other.autor != null)
				return false;
		} else if (!autor.equals(other.autor))
			return false;
		if (categoria == null) {
			if (other.categoria != null)
				return false;
		} else if (!categoria.equals(other.categoria))
			return false;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (fechaRegistro == null) {
			if (other.fechaRegistro != null)
				return false;
		} else if (!fechaRegistro.equals(other.fechaRegistro))
			return false;
		if (fecha_publicacion == null) {
			if (other.fecha_publicacion != null)
				return false;
		} else if (!fecha_publicacion.equals(other.fecha_publicacion))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (locales == null) {
			if (other.locales != null)
				return false;
		} else if (!locales.equals(other.locales))
			return false;
		if (prestamos == null) {
			if (other.prestamos != null)
				return false;
		} else if (!prestamos.equals(other.prestamos))
			return false;
		if (titulo == null) {
			if (other.titulo != null)
				return false;
		} else if (!titulo.equals(other.titulo))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
