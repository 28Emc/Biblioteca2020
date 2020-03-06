package com.biblioteca2020.models.service;

import java.util.List;
import javax.validation.ConstraintViolationException;
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
	public void save(Local local) throws Exception {
		if (verificarDireccion(local)) {
			localDao.save(local);
		}
	}

	@Override
	@Transactional()
	public void update(Local local) {
		localDao.save(local);
	}

	@Override
	@Transactional(readOnly = true)
	public Local findOne(Long id) throws Exception {
		return localDao.findById(id).orElseThrow(() -> new Exception("El local no existe."));
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

	/*
	 * @Override
	 * 
	 * @Transactional(readOnly = true) public List<Local> fetchByIdWithLibro() {
	 * return localDao.fetchByIdWithLibro(); }
	 */

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

	@Override
	@Transactional
	public List<Local> fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado) throws Exception {
		if (localDao.fetchByIdWithEmpresaWithEmpleado(idEmpresa, idEmpleado).isEmpty()) {
			throw new Exception("No tienes acceso a otros locales.");
		}
		return localDao.fetchByIdWithEmpresaWithEmpleado(idEmpresa, idEmpleado);
	}

	@Override
	@Transactional
	public List<Local> findOnlyById(Long id) {
		return localDao.findOnlyById(id);
	}

	@Override
	@Transactional
	public Local findById(Long id) throws Exception {
		return localDao.findById(id).orElseThrow(() -> new Exception("El local no existe."));
	}

}
