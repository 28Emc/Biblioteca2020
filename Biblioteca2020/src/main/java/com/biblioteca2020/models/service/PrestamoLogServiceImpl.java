package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca2020.models.dao.IPrestamoLogDao;
import com.biblioteca2020.models.entity.PrestamoLog;

@Service
public class PrestamoLogServiceImpl implements IPrestamoLogService {

	@Autowired
	private IPrestamoLogDao prestamoLogDao;

	@Override
	@Transactional(readOnly = true)
	public PrestamoLog findById(Long id) {
		return prestamoLogDao.findById(id).get();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PrestamoLog> findAll() {
		return (List<PrestamoLog>) prestamoLogDao.findAll();
	}

	// USADO
	@Override
	@Transactional
	public void save(PrestamoLog prestamoLog) {
		prestamoLogDao.save(prestamoLog);
	}
}
