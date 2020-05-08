package com.biblioteca2020.models.service;

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ILocalDao;
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Local;

@Service
public class LocalServiceImpl implements ILocalService {

	@Autowired
	private ILocalDao localDao;

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Local> findAll() {
		return (List<Local>) localDao.findAll();
	}

	// USADO
	@Override
	@Transactional
	public void save(Local local) throws Exception {
		if (verificarDireccion(local)) {
			localDao.save(local);
		}
	}

	// USADO
	@Override
	@Transactional()
	public void update(Local local) {
		localDao.save(local);
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
	public Local findByDireccion(String direccion) {
		return localDao.findByDireccion(direccion);
	}

	public boolean verificarDireccion(Local local) throws ConstraintViolationException {
		Local localEncontrado = findByDireccion(local.getDireccion());
		if (localEncontrado != null) {
			throw new ConstraintViolationException("La dirección ya está en uso.", null);
		}
		return true;
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Local fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado) throws Exception {
		if (localDao.fetchByIdWithEmpresaWithEmpleado(idEmpresa, idEmpleado) == null) {
			throw new Exception("No tienes acceso a otros locales.");
		}
		return localDao.fetchByIdWithEmpresaWithEmpleado(idEmpresa, idEmpleado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Local findById(Long id) throws Exception {
		return localDao.findById(id).orElseThrow(() -> new Exception("El local no existe."));
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Local findFirstByEmpresa(Empresa empresa) {
		return localDao.findFirstByEmpresa(empresa);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Local fetchByIdWithEmpresa(Long id) {
		return localDao.fetchByIdWithEmpresa(id);
	}

}
