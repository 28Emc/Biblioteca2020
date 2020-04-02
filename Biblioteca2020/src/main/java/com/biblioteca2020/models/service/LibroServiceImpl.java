package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ILibroDao;
import com.biblioteca2020.models.entity.Libro;

@Service
public class LibroServiceImpl implements ILibroService {

	@Autowired
	private ILibroDao libroDao;
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findAll() {
		return (List<Libro>) libroDao.findAll();
	}

	@Override
	@Transactional
	public void save(Libro libro) {
		libroDao.save(libro);
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public Libro findOne(Long id) throws Exception {
		return libroDao.findById(id).orElseThrow(() -> new Exception("El libro no existe."));
	}

	/*@Override
	@Transactional
	public void delete(Long id) {
		libroDao.deleteById(id);
	}*/

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByEstado(Boolean estado) {
		return libroDao.findByEstado(estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTitulo(String term) {
		return libroDao.findByTitulo(term);
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloLikeIgnoreCaseAndLocalAndEstado(String term, Long id, Boolean estado) {
		return libroDao.findByTituloLikeIgnoreCaseAndLocalAndEstado("%" + term + "%", id, estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado) {
		return libroDao.fetchByIdWithLocalesWithEmpleado(id, idEmpleado);
	}

	@Override
	@Transactional
	public void update(Libro libro) {
		libroDao.save(libro);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloGroup() {
		return libroDao.findByTituloGroup();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloLikeIgnoreCase(String titulo) {
		return libroDao.findByTituloLikeIgnoreCase(titulo);
	}

	@Override
	@Transactional(readOnly = true)
	public Libro findByTituloAndLocalAndEstado(String term, Long id, Boolean estado) {
		return libroDao.findByTituloAndLocalAndEstado(term, id, estado);
	}
}