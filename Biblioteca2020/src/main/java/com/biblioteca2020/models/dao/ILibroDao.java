package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroDao extends CrudRepository<Libro, Long> {

	public List<Libro> findByEstado(Boolean estado);

	// MÈTODO PARA CARGAR LIBROS CON AUTOCOMPLETADO
	@Query("select l from Libro l where l.titulo like ?1")
	public List<Libro> findByTitulo(String term);
	
	@Query("select l from Libro l where l.titulo like ?1 and l.estado = ?2")
	public List<Libro> findByTituloLikeIgnoreCaseAndEstado(String term, Boolean estado);
	
	@Query("select l from Libro l join fetch l.local lo join fetch lo.empleados em where lo.id=?1 and em.id=?2")
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado);
}