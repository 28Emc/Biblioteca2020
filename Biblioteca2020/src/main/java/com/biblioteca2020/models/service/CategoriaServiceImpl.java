package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ICategoriaDao;
import com.biblioteca2020.models.entity.Categoria;

@Service
public class CategoriaServiceImpl implements ICategoriaService {

	@Autowired
	private ICategoriaDao categoriaDao;
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Categoria> findAll() {
		return (List<Categoria>) categoriaDao.findAll();
	}
	// USADO
	public boolean verificarCategoria(Categoria categoria) throws Exception {
		Categoria categoriaEncontrada = categoriaDao.findByNombre(categoria.getNombre());
		if (categoriaEncontrada != null) {
			throw new Exception("La categoría ya existe.");
		}
		return true;
	}
	// USADO
	@Override
	@Transactional
	public void save(Categoria categoria) throws Exception {
		if (verificarCategoria(categoria)) {
			categoriaDao.save(categoria);
		}
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public Categoria findOne(Long id) throws Exception {
		return categoriaDao.findById(id).orElseThrow(() -> new Exception("La categoría no exsiste."));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		categoriaDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Categoria> findByEstado(Boolean estado) {
		return categoriaDao.findByEstado(estado);
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Categoria> findByNombreLikeIgnoreCase(String term) {
		return categoriaDao.findByNombreLikeIgnoreCase("%" + term + "%");
	}
	// USADO
	@Override
	@Transactional
	public void update(Categoria categoria) {
		categoriaDao.save(categoria);
	}

	@Override
	@Transactional
	public Categoria findByNombre(String categoria) {
		return categoriaDao.findByNombre(categoria);
	}

}
