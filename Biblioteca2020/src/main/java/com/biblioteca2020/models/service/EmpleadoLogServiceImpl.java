package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca2020.models.dao.IEmpleadoLogDao;
import com.biblioteca2020.models.entity.EmpleadoLog;

@Service
public class EmpleadoLogServiceImpl implements IEmpleadoLogService {

	@Autowired
	private IEmpleadoLogDao empleadoLogDao;

	@Override
	@Transactional(readOnly = true)
	public List<EmpleadoLog> findAll() {
		return (List<EmpleadoLog>) empleadoLogDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public EmpleadoLog findById(Long id) {
		return empleadoLogDao.findById(id).get();
	}

	@Override
	@Transactional
	public void save(EmpleadoLog empleado) {
		empleadoLogDao.save(empleado);
	}

}
