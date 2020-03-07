package com.biblioteca2020.models.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Local;

public interface ILocalDao extends CrudRepository<Local, Long> {
	
	public Local findFirstByEmpresa(Empresa empresa);

	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);
	
	public Local findByDireccion(String direccion);

	/*@Query("select l from Local l join fetch l.libros li")
	public List<Local> fetchByIdWithLibro();*/
	
	public Optional<Local> findById(Long id);
	
	@Query("select l from Local l where l.id=?1")
	public List<Local> findOnlyById(Long id);
	
	//ESTA QUERY MUESTRA LOS LOCALES ANEXOS A LA EMPRESA Y AL EMPLEADO QUE ESTÁ LOGUEADO EN ESE MOMENTO
	@Query("select l from Local l join fetch l.empleados e join fetch l.empresa em where em.id=?1 and e.id=?2")
	public List<Local> fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado);

}
