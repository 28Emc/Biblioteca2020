package com.biblioteca2020.models.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empleado;

public interface IEmpleadoDao extends CrudRepository<Empleado, Long> {

	public Optional<Empleado> findById(Long id);

	public Empleado findByUsername(String username);

	public Empleado findByNroDocumento(String nroDocumento);

	public Empleado findByUsernameAndEstado(String username, boolean estado);
	
	@Query("select e from Empleado e join fetch e.local l join fetch l.empresa em where em.id=?1")
	public List<Empleado> fetchByIdWithLocalWithEmpresa(Long id);
	
	@Query("select e from Empleado e join fetch e.roles r join fetch e.local l join fetch l.empresa em where em.id=?1 and r.authority not like 'ROLE_ADMIN'")
	public List<Empleado> fetchByIdWithLocalWithEmpresaNotAdmin(Long id);

	@Query("select e from Empleado e join fetch e.roles r where r.authority='ROLE_EMPLEADO'")
	public List<Empleado> fetchByIdWithRoles();
	
	@Query("select e from Empleado e join fetch e.roles r where r.authority in ('ROLE_EMPLEADO', 'ROLE_SUPERVISOR')")
	public List<Empleado> fetchByIdWithRolesSupervisor();

	@Query("select e from Empleado e where e.username not in ?1")
	public List<Empleado> findByAnotherUsername(String username);

	public Empleado findByCelular(String celular);

}
