package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ILibroDao;
import com.biblioteca2020.models.dao.IPrestamoDao;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;

@Service
public class PrestamoServiceImpl implements IPrestamoService {

	@Autowired
	private IPrestamoDao prestamoDao;

	@Autowired
	private ILibroDao libroDao;

	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> findAll() {
		return (List<Prestamo>) prestamoDao.findAll();
	}

	@Override
	@Transactional
	public void save(Prestamo prestamo) {
		prestamoDao.save(prestamo);
	}

	@Override
	@Transactional(readOnly = true)
	public Prestamo findById(Long id) {
		return prestamoDao.findById(id).orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> findByDevolucion(String devolucion) {
		return prestamoDao.findByDevolucion(devolucion);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTitulo(String term) {
		return libroDao.findByTitulo(term);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> fetchPrestamoByIdWithLibroWithUsuario() {
		return prestamoDao.fetchByIdWithLibroWithUsuario();
	}

}
