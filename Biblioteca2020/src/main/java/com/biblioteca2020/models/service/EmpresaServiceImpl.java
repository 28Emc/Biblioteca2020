package com.biblioteca2020.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IEmpresaDao;
import com.biblioteca2020.models.entity.Empresa;

@Service
public class EmpresaServiceImpl implements IEmpresaService {

	@Autowired
	private IEmpresaDao empresaDao;

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empresa findOne(Long id) throws Exception {
		return empresaDao.findById(id).orElseThrow(() -> new Exception("La empresa con id " + id + " no existe."));
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empresa findByRucAndEstado(String ruc, boolean estado) {
		return empresaDao.findByRucAndEstado(ruc, estado);
	}

	/*
	 * private boolean verificarRuc(Empresa empresa) throws
	 * ConstraintViolationException { Empresa empresaEncontrada =
	 * empresaDao.findByRuc(empresa.getRuc()); if (empresaEncontrada != null) {
	 * throw new ConstraintViolationException("El ruc ya está en uso.", null); }
	 * return true; }
	 * 
	 * private boolean verificarRazonSocial(Empresa empresa) throws
	 * ConstraintViolationException { Empresa empresaEncontrada =
	 * empresaDao.findByRazonSocial(empresa.getRazonSocial()); if (empresaEncontrada
	 * != null) { throw new
	 * ConstraintViolationException("La razón social ya existe.", null); } return
	 * true; }
	 * 
	 * @Override
	 * 
	 * @Transactional public void save(Empresa empresa) throws Exception { if
	 * (verificarRazonSocial(empresa) && verificarRuc(empresa)) {
	 * empresaDao.save(empresa); } }
	 */
	// USADO
	@Override
	@Transactional
	public void update(Empresa empresa) throws Exception {
		empresaDao.save(empresa);
	}
}
