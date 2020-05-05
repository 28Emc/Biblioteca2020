package com.biblioteca2020.models.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empleado;

public interface IEmpleadoDao extends CrudRepository<Empleado, Long> {
	// USADO
	public Optional<Empleado> findById(Long id);

	// USADO
	public Empleado findByUsername(String username);

	// USADO
	public Empleado findByNroDocumento(String nroDocumento);

	// USADO
	@Query("select e from Empleado e where e.nroDocumento like ?1 and e.estado = ?2")
	public List<Empleado> findAllByNroDocumentoAndEstado(String term, boolean estado);

	// USADO
	public Empleado findByUsernameAndEstado(String username, boolean estado);

	// USADO
	@Query("select e from Empleado e where e.username like ?1 and e.local.id=?2")
	public Empleado findByUsernameAndLocal(String username, Long id_local);

	// USADO
	@Query("select e from Empleado e where e.local.id like ?1")
	public List<Empleado> findByLocal(Long id_local);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r where r.authority like ?1 and e.local.id=?2")
	public Empleado findByRoleAndLocal(String role, Long id_local);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r where r.authority in ('ROLE_EMPLEADO') and e.local.id=?1")
	public Empleado findByRoleAndLocalNotAdmin(Long id_local);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r join fetch e.local l join fetch l.empresa em where l.id=?1 and r.authority not in ('ROLE_PRUEBA')")
	public List<Empleado> fetchByIdWithLocalWithEmpresa(Long id);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r join fetch e.local l join fetch l.empresa em where em.id=?1 and r.authority not like 'ROLE_ADMIN'")
	public List<Empleado> fetchByIdWithLocalWithEmpresaNotAdmin(Long id);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r where r.authority='ROLE_EMPLEADO'")
	public List<Empleado> fetchByIdWithRoles();

	// USADO
	@Query("select e from Empleado e join fetch e.roles r where r.authority in ('ROLE_ADMIN', 'ROLE_EMPLEADO') and e.nroDocumento like ?1")
	public List<Empleado> fetchByIdWithRoles(String term);

	// USADO
	@Query("select e from Empleado e join fetch e.roles r where e.username NOT LIKE '%Prueba%' and e.nroDocumento like ?1")
	public List<Empleado> fetchByIdWithRolesSysAdmin(String term);

	// USADO
	public Empleado findByCelular(String celular);

}
