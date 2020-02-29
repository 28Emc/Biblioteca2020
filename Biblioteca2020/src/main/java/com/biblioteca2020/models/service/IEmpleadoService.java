package com.biblioteca2020.models.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import com.biblioteca2020.models.entity.Empleado;

public interface IEmpleadoService {

	public List<Empleado> findAll();
	
	public List<Empleado> fetchByIdWithRoles();
	
	public List<Empleado> fetchByIdWithRolesSupervisor();
	
	public Empleado findById(Long id) throws Exception;

	public Empleado findByUsername(String username);

	public Empleado findByUsernameAndEstado(String username, boolean estado);
	
	public Empleado findByNroDocumento(String nroDocumento);
		
	public void save(Empleado empleado) throws Exception;
	
	public void update(Empleado empleado) throws Exception;
	
	public void borrarEmpleado(Long id) throws Exception;
		
	public void isAdminListar(Model model, Principal principal);

	public void isAdminEditar(Map<String, Object> model, Principal principal);
	
}