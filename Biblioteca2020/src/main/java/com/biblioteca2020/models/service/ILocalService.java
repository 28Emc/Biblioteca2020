package com.biblioteca2020.models.service;

import java.util.List;

import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Local;

public interface ILocalService {

	public List<Local> findAll();

	// USADO
	public void save(Local local) throws Exception;

	// USADO
	public void update(Local local) throws Exception;

	// USADO
	public Local findById(Long id) throws Exception;

	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);

	public Local findByDireccion(String direccion);

	// USADO
	public List<Local> findFirstByEmpresa(Empresa empresa);

	// USADO
	public Local fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado) throws Exception;

	// USADO
	public Local fetchByIdWithEmpresa(Long id);

	// USADO
	public List<Local> fetchByIdWithEmpresa();

}
