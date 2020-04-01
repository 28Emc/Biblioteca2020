package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Role;

public interface IRoleService {
	public List<Role> findAll();
	// USADO
	public List<Role> findOnlyUsers();
	// USADO
	public List<Role> findEmpleadoAndSupervisor();
}
