package com.biblioteca2020.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Role;

public interface IRoleDao extends CrudRepository<Role, Long> {
	
	public List<Role> findAll();

	@Query("select r from Role r where r.authority not like 'ROLE_ADMIN'")
	public List<Role> findNotAdmins();
	
}
