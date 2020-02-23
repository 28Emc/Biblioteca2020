package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ILocalDao;
import com.biblioteca2020.models.entity.Local;

@Service
public class LocalServiceImpl implements ILocalService {

	@Autowired
	private ILocalDao localDao;

	@Override
	@Transactional(readOnly = true)
	public List<Local> findAll() {
		return (List<Local>) localDao.findAll();
	}

	@Override
	@Transactional
	public void save(Local local) {
		localDao.save(local);
	}

	@Override
	@Transactional(readOnly = true)
	public Local findOne(Long id) {
		return localDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		localDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Local> findByEstado(Boolean estado) {
		return localDao.findByEstado(estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Local> findByDireccionLikeIgnoreCase(String term) {
		return localDao.findByDireccionLikeIgnoreCase("%" + term + "%");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Local> fetchByIdWithLibro() {
		return localDao.fetchByIdWithLibro();
	}

}
