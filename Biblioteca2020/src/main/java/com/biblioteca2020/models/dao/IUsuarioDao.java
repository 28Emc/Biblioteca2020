package com.biblioteca2020.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {
	
	public Usuario findByUsername(String username);

	public Usuario findByUsernameAndEstado(String username, boolean estado);
}
