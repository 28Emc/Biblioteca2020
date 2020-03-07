package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Categoria;

public interface ICategoriaService {
	
	public List<Categoria> findAll();

	public void save(Categoria categoria) throws Exception;

	public Categoria findOne(Long id) throws Exception;

	public void delete(Long id);
	
	public List<Categoria> findByEstado(Boolean estado);
	
	public List<Categoria> findByNombreLikeIgnoreCase(String string);
	
	public Categoria findByNombre(String categoria);

	public void update(Categoria categoria);
	
}