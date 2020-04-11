package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {
	// USADO
	public List<Usuario> findAll();
	// USADO	
	public Usuario findById(Long id) throws Exception;
	// USADO
	public Usuario findByUsername(String username);
	// USADO
	public Usuario findByUsernameAndEstado(String username, boolean estado);
	// USADO
	public Usuario findByNroDocumento(String nroDocumento);
	// USADO
	public Usuario findByNroDocumentoAndEmailAndEstado(String nroDocumento, String email, boolean estado);
	// USADO
	public List<Usuario> findByNroDocumentoAndEstado(String term, boolean estado);	
	// USADO	
	public void save(Usuario usuario) throws Exception;
	// USADO
	public void update(Usuario usuario) throws Exception;
	// USADO
	public Usuario cambiarPassword(CambiarPassword form) throws Exception;
	
	public void borrarUsuario(Long id) throws Exception;
	// USADO
	public Usuario findByEmailIgnoreCase(String email);
}
