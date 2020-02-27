package com.biblioteca2020.models.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {

	public List<Usuario> findAll();
	
	public List<Usuario> fetchByIdWithRoles();
	
	public Usuario findById(Long id) throws Exception;

	public Usuario findByUsername(String username);

	public Usuario findByUsernameAndEstado(String username, boolean estado);
	
	public Usuario findByNroDocumento(String nroDocumento);
		
	public void save(Usuario usuario) throws Exception;
	
	public void update(Usuario usuario) throws Exception;
	
	public void borrarUsuario(Long id) throws Exception;
		
	public void isAdminListar(Model model, Principal principal);

	public void isAdminEditar(Map<String, Object> model, Principal principal);
	
}
