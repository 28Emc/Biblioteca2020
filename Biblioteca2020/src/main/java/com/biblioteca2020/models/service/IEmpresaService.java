package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaService {
	public List<Empresa> findAll();

	public Empresa findByEstado(Boolean estado);
	// USADO
	public Empresa findByRucAndEstado(String ruc, boolean estado);
	// USADO
	public Empresa findOne(Long id) throws Exception;
	// USADO
	public void update(Empresa empresa) throws Exception;
}
