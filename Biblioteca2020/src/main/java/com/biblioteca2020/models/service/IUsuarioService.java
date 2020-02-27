package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {

	public List<Usuario> findAll();

	public Usuario findByUsername(String username);

	public Usuario findByUsernameAndEstado(String username, boolean estado);
	
	public Usuario findByNroDocumento(String nroDocumento);
		
	public void saveNew(Usuario usuario) throws Exception;
	
	public void save(Usuario usuario) throws Exception;

	public Usuario findById(Long id) throws Exception;
	
	public void borrarUsuario(Long id) throws Exception;
	
	public List<Usuario> fetchByIdWithRoles();
	
}
