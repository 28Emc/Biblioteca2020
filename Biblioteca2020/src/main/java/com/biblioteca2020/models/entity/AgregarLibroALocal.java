package com.biblioteca2020.models.entity;

import javax.validation.constraints.NotNull;

public class AgregarLibroALocal {

	@NotNull
	private Long localId;
	
	@NotNull
	private Long libroId;
	
	private Iterable<Libro> libros;
	
	private Local local;

	public AgregarLibroALocal() {
	}

	public AgregarLibroALocal(Iterable<Libro> libros, Local local) {
		this.libros = libros;
		this.local = local;
	}

	public Long getLocalId() {
		return localId;
	}

	public void setLocalId(Long localId) {
		this.localId = localId;
	}

	public Long getLibroId() {
		return libroId;
	}

	public void setLibroId(Long libroId) {
		this.libroId = libroId;
	}

	public Iterable<Libro> getLibros() {
		return libros;
	}

	public void setLibros(Iterable<Libro> libros) {
		this.libros = libros;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}
}
