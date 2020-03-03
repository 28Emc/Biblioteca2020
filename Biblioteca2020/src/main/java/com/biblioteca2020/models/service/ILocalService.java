package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Local;

public interface ILocalService {
	
	public List<Local> findAll();

	public void save(Local local) throws Exception;
	
	public void update(Local local) throws Exception;

	public Local findOne(Long id) throws Exception;

	public void delete(Long id);
	
	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);
	
	public Local findByDireccion(String direccion);
	
	public List<Local> fetchByIdWithLibro();
	
	public List<Local> findOnlyById(Long id);
	
	public List<Local> fetchByIdWithEmpresaWithEmpleado(Long idEmpresa, Long idEmpleado) throws Exception;

}
