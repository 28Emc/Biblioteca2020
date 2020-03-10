package com.biblioteca2020.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaDao extends CrudRepository<Empresa, Long> {

	public Empresa findByRuc(String ruc);

	public Empresa findByRazonSocial(String razonSocial);
	
	public Empresa findByEstado(Boolean estado);
	
	@Query("select e from Empresa e where e.ruc like %?1% and e.estado=?2")
	public Empresa findByRucAndEstado(String ruc, boolean estado);
}
