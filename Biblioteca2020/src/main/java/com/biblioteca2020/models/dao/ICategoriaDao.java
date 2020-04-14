package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Categoria;

public interface ICategoriaDao extends CrudRepository<Categoria, Long> {
	// USADO
	public List<Categoria> findByNombreLikeIgnoreCase(String string);

	// USADO
	public Categoria findByNombre(String categoria);
}
