package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.UsuarioLog;

public interface IUsuarioLogService {
	public List<UsuarioLog> findAll();
	public UsuarioLog findById(Long id);

	// USADO
	public void save(UsuarioLog usuarioLog);

}
