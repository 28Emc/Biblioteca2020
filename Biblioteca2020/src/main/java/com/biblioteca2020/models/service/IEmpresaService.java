package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaService {
	
	public List<Empresa> findAll();

	public void save(Empresa empresa);

	public Empresa findOne(Long id);

	public void delete(Long id);
	
	public List<Empresa> findByEstado(Boolean estado);
}
