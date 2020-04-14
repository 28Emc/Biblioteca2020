package com.biblioteca2020.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Role;

public interface IRoleDao extends CrudRepository<Role, Long> {

	// USADO
	@Query("select r from Role r where r.authority like 'ROLE_USER'")
	public List<Role> findOnlyUsers();

	// USADO
	@Query("select r from Role r where r.authority like 'ROLE_EMPLEADO'")
	public List<Role> findOnlyEmpleados();

	// USADO
	@Query("select r from Role r where r.authority in ('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	public List<Role> findForEmpleadosAndAdmin();

	// USADO
	@Query("select r from Role r where r.authority not in ('ROLE_PRUEBA', 'ROLE_USER')")
	public List<Role> findForSysadmin();
}
