package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroService {
	// USADO
	public List<Libro> findAll();

	public void save(Libro libro);
	// USADO
	public Libro findOne(Long id) throws Exception;

	public List<Libro> findByEstado(Boolean estado);
    // USADO
	public List<Libro> findByTituloGroup();
    // USADO
	public List<Libro> findByTitulo(String term);
	// USADO
	public List<Libro> findByTituloLikeIgnoreCase(String titulo);
	// USADO
	public List<Libro> findByTituloLikeIgnoreCaseAndLocalAndEstado(String term, Long id, Boolean estado);
	// USADO
	public Libro findByTituloAndLocalAndEstado(String term, Long id, Boolean estado);
	// USADO
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado);
	
	public void update(Libro libro);

}
