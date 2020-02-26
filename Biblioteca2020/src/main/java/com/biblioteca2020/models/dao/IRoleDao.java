package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Role;

public interface IRoleDao extends CrudRepository<Role, Long> {
	
	public List<Role> findAll();

}
