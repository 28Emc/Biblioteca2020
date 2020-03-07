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

	@Override
	@Transactional(readOnly = true)
	public Libro findOne(Long id) {
		return libroDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		libroDao.deleteById(id);
	}

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

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTituloLikeIgnoreCase(String term) {
		return libroDao.findByTituloLikeIgnoreCase("%" + term + "%");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> fetchByIdWithLocalesWithEmpleado(Long id, Long idEmpleado) throws Exception {
		if (libroDao.fetchByIdWithLocalesWithEmpleado(id, idEmpleado).isEmpty()) {
			throw new Exception("No tienes acceso a estos libros ya que pertenecen a un local sin permiso de acceso.");
		}
		return libroDao.fetchByIdWithLocalesWithEmpleado(id, idEmpleado);
	}

	/*@Override
	@Transactional(readOnly = true)
	public List<Libro> fetchByIdWithLocalWithEmpresaWithEmpleado(Long idLocal, Long idEmpleado) throws Exception {
		if (libroDao.fetchByIdWithLocalWithEmpresaWithEmpleado(idLocal, idEmpleado).isEmpty()) {
			throw new Exception("No tienes acceso a estos libros ya que pertenecen a un local sin permiso de acceso.");
		}
		if (libroDao.fetchByIdWithLocalWithEmpresaWithEmpleado(idLocal, idEmpleado) == null) {
			throw new Exception("El local no tiene libros.");
		}
		return libroDao.fetchByIdWithLocalWithEmpresaWithEmpleado(idLocal, idEmpleado);
	}*/

	@Override
	@Transactional
	public void update(Libro libro) {
		libroDao.save(libro);
	}

}
