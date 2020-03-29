package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {

	public List<Usuario> findAll();
		
	public Usuario findById(Long id) throws Exception;

	public Usuario findByUsername(String username);
	
	public Usuario findByUsernameAndEstado(String username, boolean estado);
	
	public Usuario findByNroDocumento(String nroDocumento);
	
	public List<Usuario> findAllByNroDocumentoAndEstado(String term, Boolean estado);	
		
	public void save(Usuario usuario) throws Exception;
	
	public void update(Usuario usuario) throws Exception;
	
	public Usuario cambiarPassword(CambiarPassword form) throws Exception;
	
	public void borrarUsuario(Long id) throws Exception;
	
	public Usuario findByEmailIgnoreCase(String email);
}
