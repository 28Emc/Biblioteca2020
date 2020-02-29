package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IRoleDao;
import com.biblioteca2020.models.entity.Role;

@Service
public class RoleServiceImpl implements IRoleService {

	@Autowired
	private IRoleDao roleDao;

	@Override
	@Transactional(readOnly = true)
	public List<Role> findAll() {
		return roleDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> findOnlyUsers() {
		return roleDao.findOnlyUsers();
	}

}
