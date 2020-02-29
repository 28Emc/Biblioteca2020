package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Empresa;

public interface IEmpresaDao extends CrudRepository<Empresa, Long> {

	public Empresa findByRuc(String ruc);

	public Empresa findByRazonSocial(String razonSocial);
	
	public List<Empresa> findByEstado(Boolean estado);
	
	@Query("select e from Empresa e where e.ruc like %?1% and e.estado=?2")
	public List<Empresa> findByRucAndEstado(String ruc, boolean estado);

	@Query("select e from Empresa e join fetch e.empleados em where em.id=?1")
	public Empresa fetchByIdWithEmpleado(Long id);

}
