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

	// USADO
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

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloLikeIgnoreCaseAndLocalAndEstado(String term, Long id, Boolean estado) {
		return libroDao.findByTituloLikeIgnoreCaseAndLocalAndEstado("%" + term + "%", id, estado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado) {
		return libroDao.fetchByIdWithLocalesWithEmpleado(id, idEmpleado);
	}

	// USADO
	@Override
	@Transactional
	public void update(Libro libro) {
		libroDao.save(libro);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloGroup() {
		return libroDao.findByTituloGroup();
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloLikeIgnoreCase(String titulo) {
		return libroDao.findByTituloLikeIgnoreCase(titulo);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Libro findByTituloAndLocalAndEstado(String term, Long id, Boolean estado) {
		return libroDao.findByTituloAndLocalAndEstado(term, id, estado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByCategoriaAndLocal(String categoria, Long localId) {
		return libroDao.findByCategoriaAndLocal("%" + categoria + "%", localId);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByEstado(boolean estado) {
		return libroDao.findByEstado(estado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByLocalAndEstado(Long idLocal, boolean estado) {
		return libroDao.findByLocalAndEstado(idLocal, estado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> fetchWithCategoriaWithLocal() {
		return libroDao.fetchWithCategoriaWithLocal();
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByLocal(Long idLocal) {
		return libroDao.findByLocal(idLocal);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByCategoria(String categoria) {
		return libroDao.findByCategoria(categoria);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Libro findByTituloAndLocal(String term, Long id) {
		return libroDao.findByTituloAndLocal(term, id);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloDistinct(String term) {
		return libroDao.findByTituloDistinct("%" + term + "%");
	}
}