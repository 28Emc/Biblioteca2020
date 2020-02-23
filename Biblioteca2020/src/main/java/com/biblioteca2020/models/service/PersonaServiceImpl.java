package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IPersonaDao;
import com.biblioteca2020.models.entity.Persona;

@Service
public class PersonaServiceImpl implements IPersonaService {

	@Autowired
	private IPersonaDao personaDao;

	@Override
	@Transactional(readOnly = true)
	public List<Persona> findAll() {
		return (List<Persona>) personaDao.findAll();
	}
	
	@Override
	public List<Persona> findByEstado(Boolean estado) {
		return personaDao.findByEstado(estado);
	}
	
	@Override
	@Transactional
	public void save(Persona persona) {
		personaDao.save(persona);
	}

	@Override
	@Transactional(readOnly = true)
	public Persona findOne(Long id) {
		return personaDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		personaDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Persona> findByNombres(String term) {
		return personaDao.findByNombres(term);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Persona> findByNombresLikeIgnoreCase(String term) {
		return personaDao.findByNombresLikeIgnoreCase("%" + term + "%");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Persona> findByNro_documento(String term) {
		return personaDao.findByNro_documento("%" + term + "%");
	}

}
