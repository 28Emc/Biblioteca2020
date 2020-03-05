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
	
	@Override	
	@Transactional(readOnly = true)
	public List<Categoria> findAll() {
		return (List<Categoria>) categoriaDao.findAll();
	}

	@Override
	@Transactional
	public void save(Categoria categoria) {
		categoriaDao.save(categoria);
	}

	@Override
	@Transactional(readOnly = true)
	public Categoria findOne(Long id) {
		return categoriaDao.findById(id).orElse(null);
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

	@Override
	@Transactional(readOnly = true)
	public List<Categoria> findByNombreLikeIgnoreCase(String term) {
		return categoriaDao.findByNombreLikeIgnoreCase("%" + term + "%");
	}

}
