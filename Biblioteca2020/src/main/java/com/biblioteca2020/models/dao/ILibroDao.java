package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Libro;

public interface ILibroDao extends CrudRepository<Libro, Long> {

	// USADO
	// MUESTRO SOLO LOS LIBROS SIN REPETIRSE
	@Query(value = "select * from libros group by titulo", nativeQuery = true)
	public List<Libro> findByTituloGroup();

	// USADO
	// VER DISPONIBILIDAD LIBRO POR TITULO Y LOCAL
	@Query("select l from Libro l join fetch l.local lo where l.titulo like ?1")
	public List<Libro> findByTituloLikeIgnoreCase(String titulo);

	// USADO
	@Query("select l from Libro l join fetch l.local lo where l.titulo like ?1 and lo.id = ?2 and l.estado = ?3")
	public List<Libro> findByTituloLikeIgnoreCaseAndLocalAndEstado(String term, Long id, Boolean estado);

	// USADO
	@Query("select l from Libro l join fetch l.local lo where l.titulo like ?1 and lo.id = ?2 and l.estado = ?3")
	public Libro findByTituloAndLocalAndEstado(String term, Long id, Boolean estado);

	// USADO
	@Query("select l from Libro l join fetch l.local lo join fetch lo.empleados em where lo.id=?1 and em.id=?2")
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado);
}