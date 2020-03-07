package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroDao extends CrudRepository<Libro, Long> {

	public List<Libro> findByEstado(Boolean estado);

	// MÈTODO PARA CARGAR LIBROS CON AUTOCOMPLETADO
	@Query("select l from Libro l where l.titulo like %?1%")
	public List<Libro> findByTitulo(String term);
	
	public List<Libro> findByTituloLikeIgnoreCase(String term);
	
	@Query("select l from Libro l join fetch l.locales lo join fetch lo.empleados em where lo.id=?1 and em.id=?2")
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado);
	
	/*@Query("select l from Libro l join fetch l.locales lo join fetch lo.empresa e join fetch e.empleados em where lo.id=?1 and em.id=?2")
	public List<Libro> fetchByIdWithLocalWithEmpresaWithEmpleado(Long idLocal, Long idEmpleado);*/

}