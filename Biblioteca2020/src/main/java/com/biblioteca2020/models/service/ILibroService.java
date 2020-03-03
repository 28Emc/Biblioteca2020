package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroService {

	public List<Libro> findAll();

	public void save(Libro libro);

	public Libro findOne(Long id);

	public void delete(Long id);

	public List<Libro> findByEstado(Boolean estado);

	public List<Libro> findByTitulo(String term);
	
	public List<Libro> findByTituloLikeIgnoreCase(String term);
	
	public List<Libro> fetchByIdWithLocales(Long id);
	
	public List<Libro> fetchByIdWithLocalWithEmpresaWithEmpleado(Long idLocal, Long idEmpleado) throws Exception;

	public void update(Libro libro);

}
