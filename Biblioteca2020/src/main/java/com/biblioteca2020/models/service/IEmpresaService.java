package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaService {
	
	public List<Empresa> findAll();

	public void save(Empresa empresa) throws Exception;
	
	public void update(Empresa empresa) throws Exception;

	public Empresa findOne(Long id) throws Exception;

	public void delete(Long id);
	
	public List<Empresa> findByEstado(Boolean estado);

}
