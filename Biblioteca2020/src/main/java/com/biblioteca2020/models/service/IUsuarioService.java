package com.biblioteca2020.models.service;

import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioService {
	
	public Usuario findByUsernameAndEstado(String username, boolean estado);
}
