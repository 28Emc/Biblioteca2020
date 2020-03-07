package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {

	public List<Usuario> findAll();
		
	public Usuario findById(Long id) throws Exception;

	public Usuario findByUsername(String username);
	
	public Usuario findByNroDocumento(String nroDocumento);
		
	public void save(Usuario usuario) throws Exception;
	
	public void update(Usuario usuario) throws Exception;
	
	public void borrarUsuario(Long id) throws Exception;	
}
