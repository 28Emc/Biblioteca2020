package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Categoria;

public interface ICategoriaService {
	// USADO
	public List<Categoria> findAll();

	// USADO
	public Categoria findOne(Long id) throws Exception;

	// USADO
	public Categoria findByNombre(String categoria);

	// USADO
	public List<Categoria> findByNombreLikeIgnoreCase(String string);

	// USADO
	public void save(Categoria categoria) throws Exception;

}