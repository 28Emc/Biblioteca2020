package com.biblioteca2020.models.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {

	public List<Usuario> findAll();

	public Optional<Usuario> findByUsername(String username);
	
	public Usuario findByUsernameAndEstado(String username, boolean estado);

	public Optional<Usuario> findByNroDocumento(String nroDocumento);
		
}
