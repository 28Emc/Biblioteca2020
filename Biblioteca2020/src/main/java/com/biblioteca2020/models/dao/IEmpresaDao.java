package com.biblioteca2020.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaDao extends CrudRepository<Empresa, Long> {

	// USADO
	@Query("select e from Empresa e where e.ruc like %?1% and e.estado like ?2")
	public Empresa findByRucAndEstado(String ruc, boolean estado);
}
