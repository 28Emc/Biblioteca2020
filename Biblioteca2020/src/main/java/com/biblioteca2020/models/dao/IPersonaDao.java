package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Persona;

public interface IPersonaDao extends CrudRepository<Persona, Long> {

	public List<Persona> findByEstado(Boolean estado);
	
	@Query("select p from Persona p where p.nombres like %?1%")
	public List<Persona> findByNombres(String term);
	
	public List<Persona> findByNombresLikeIgnoreCase(String term);

	@Query("select p from Persona p where p.nro_documento like %?1%")
	public List<Persona> findByNro_documento(String term);

}
