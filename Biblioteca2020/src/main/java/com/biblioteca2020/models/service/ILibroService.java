package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroService {
	// USADO
	public List<Libro> findAll();

	// USADO
	public void save(Libro libro);

	// USADO
	public Libro findOne(Long id) throws Exception;

	// USADO
	public List<Libro> findByCategoriaAndLocal(String categoria, Long localId);

	// USADO
	public List<Libro> findByEstado(boolean estado);

	// USADO
	public List<Libro> findByLocal(Long idLocal);

	// USADO
	public List<Libro> findByLocalAndEstado(Long idLocal, boolean estado);

	// USADO
	public List<Libro> findByTituloDistinct(String term);

	// USADO
	//public Libro findByTitulo(String titulo);
	
	// USADO
	public List<Libro> findByTituloGroup();

	// USADO
	public List<Libro> findByTituloLikeIgnoreCase(String titulo);

	// USADO
	public List<Libro> findByTituloLikeIgnoreCaseAndLocalAndEstado(String term, Long id, Boolean estado);

	// USADO
	public Libro findByTituloAndLocal(String term, Long id);

	// USADO
	public Libro findByTituloAndLocalAndEstado(String term, Long id, Boolean estado);

	// USADO
	public List<Libro> fetchWithCategoriaWithLocal();

	// USADO
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado);

	// USADO
	public void update(Libro libro);

	// USADO
	public List<Libro> findByCategoria(String categoria);

}
