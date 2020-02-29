package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Local;

public interface ILocalDao extends CrudRepository<Local, Long> {

	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);
	
	public Local findByDireccion(String direccion);

	@Query("select l from Local l join fetch l.libros li")
	public List<Local> fetchByIdWithLibro();
	
	@Query("select l from Local l join fetch l.empresa em where em.id=?1")
	public List<Local> fetchByIdWithEmpresa(Long id);

}
