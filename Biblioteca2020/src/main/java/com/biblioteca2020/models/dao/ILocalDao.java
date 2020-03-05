package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Local;

public interface ILocalDao extends CrudRepository<Local, Long> {

	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);
	
	public Local findByDireccion(String direccion);

	/*@Query("select l from Local l join fetch l.libros li")
	public List<Local> fetchByIdWithLibro();*/
	
	@Query("select l from Local l where l.id=?1")
	public List<Local> findOnlyById(Long id);
	
	//ESTA QUERY MUESTRA LOS LOCALES ANEXOS A LA EMPRESA Y AL EMPLEADO QUE ESTÁ LOGUEADO EN ESE MOMENTO
	@Query("select l from Local l join fetch l.empresa e join fetch e.empleados em where e.id=?1 and em.id=?2")
	public List<Local> fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado);

}
