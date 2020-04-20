package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.EmpleadoLog;

public interface IEmpleadoLogService {
	public List<EmpleadoLog> findAll();
	public EmpleadoLog findById(Long id);

	// USADO
	public void save(EmpleadoLog empleado);
}
