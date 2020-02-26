package com.biblioteca2020.models.service;

import java.util.List;
import java.util.Optional;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {

	public List<Usuario> findAll();

	public Optional<Usuario> findByUsername(String username);

	public Usuario findByUsernameAndEstado(String username, boolean estado);

	public boolean verificarUsuario(Usuario usuario) throws Exception;
	
	public Optional<Usuario> findByNroDocumento(String nroDocumento);
	
	public boolean verificarPassword(Usuario user) throws Exception;

	public Usuario save(Usuario usuario) throws Exception;

	public Usuario findById(Long id) throws Exception;
	
	public Usuario update(Usuario usuario) throws Exception;

}
