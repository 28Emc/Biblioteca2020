package com.biblioteca2020.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Role;

public interface IRoleDao extends CrudRepository<Role, Long> {
	
	public List<Role> findAll();

	@Query("select r from Role r where r.authority like 'ROLE_USER'")
	public List<Role> findOnlyUsers();
	
	@Query("select r from Role r where r.authority in ('ROLE_EMPLEADO', 'ROLE_SUPERVISOR')")
	public List<Role> findEmpleadoAndSupervisor();
	
}
