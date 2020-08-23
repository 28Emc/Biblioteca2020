package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.PrestamoLog;

public interface IPrestamoLogService {
	public PrestamoLog findById(Long id);
	public List<PrestamoLog> findAll();

	// USADO
	public void save(PrestamoLog prestamoLog);
}
