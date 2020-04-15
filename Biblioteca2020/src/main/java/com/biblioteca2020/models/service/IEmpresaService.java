package com.biblioteca2020.models.service;

import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaService {

	// USADO
	public Empresa findByRucAndEstado(String ruc, boolean estado);

	// USADO
	public Empresa findOne(Long id) throws Exception;

	// USADO
	public void update(Empresa empresa) throws Exception;
}
