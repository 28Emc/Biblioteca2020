package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaDao extends CrudRepository<Empresa, Long> {
	
	public List<Empresa> findByEstado(Boolean estado);
	
}
