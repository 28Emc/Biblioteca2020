package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaService {

	public List<Empresa> findAll();

	public List<Empresa> findByEstado(Boolean estado);

	public List<Empresa> findByRucAndEstado(String ruc, boolean estado);
	
	//public Empresa fetchByIdWithLocalWithEmpleado(Long id);
	
	public Empresa findOne(Long id) throws Exception;

	public void save(Empresa empresa) throws Exception;

	public void update(Empresa empresa) throws Exception;

	public void delete(Long id);

}
