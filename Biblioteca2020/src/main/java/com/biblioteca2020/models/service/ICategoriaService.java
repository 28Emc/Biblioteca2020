package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Categoria;

public interface ICategoriaService {
	// USADO
	public List<Categoria> findAll();
	// USADO
	public void save(Categoria categoria) throws Exception;
	// USADO
	public Categoria findOne(Long id) throws Exception;

	public void delete(Long id);
	
	public List<Categoria> findByEstado(Boolean estado);
	// USADO
	public List<Categoria> findByNombreLikeIgnoreCase(String string);
	
	public Categoria findByNombre(String categoria);
	// USADO
	public void update(Categoria categoria);
	
}