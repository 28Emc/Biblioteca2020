package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IEmpresaDao;
import com.biblioteca2020.models.entity.Empresa;

@Service
public class EmpresaServiceImpl implements IEmpresaService {

	@Autowired
	private IEmpresaDao empresaDao;

	@Override
	@Transactional(readOnly = true)
	public List<Empresa> findAll() {
		return (List<Empresa>) empresaDao.findAll();
	}

	@Override
	@Transactional
	public void save(Empresa empresa) {
		empresaDao.save(empresa);
	}

	@Override
	@Transactional(readOnly = true)
	public Empresa findOne(Long id) {
		return empresaDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		empresaDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Empresa> findByEstado(Boolean estado) {
		return empresaDao.findByEstado(estado);
	}

}
