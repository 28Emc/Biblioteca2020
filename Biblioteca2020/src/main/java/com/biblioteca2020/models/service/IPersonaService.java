package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Persona;

public interface IPersonaService {
	public List<Persona> findAll();

	public void save(Persona suscriptor);

	public Persona findOne(Long id);

	public void delete(Long id);
	
	public List<Persona> findByEstado(Boolean estado);
	
	public List<Persona> findByNombres(String term);

	public List<Persona> findByNombresLikeIgnoreCase(String term);

	public List<Persona> findByNro_documento(String term);

}
