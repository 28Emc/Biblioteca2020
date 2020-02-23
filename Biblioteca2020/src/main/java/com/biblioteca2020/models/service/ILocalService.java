package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Local;

public interface ILocalService {
	
	public List<Local> findAll();

	public void save(Local local);

	public Local findOne(Long id);

	public void delete(Long id);
	
	public List<Local> findByEstado(Boolean estado);

	public List<Local> findByDireccionLikeIgnoreCase(String term);
	
	public List<Local> fetchByIdWithLibro();
}
