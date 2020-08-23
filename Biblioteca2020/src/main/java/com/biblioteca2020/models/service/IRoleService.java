package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Role;

public interface IRoleService {

	// USADO
	public List<Role> findOnlyUsers();

	// USADO
	public List<Role> findOnlyEmpleados();

	// USADO
	public List<Role> findForEmpleadosAndAdmin();

	// USADO
	public List<Role> findForSysadmin();
}
