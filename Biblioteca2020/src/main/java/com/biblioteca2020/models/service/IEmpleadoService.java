package com.biblioteca2020.models.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Empleado;

public interface IEmpleadoService {
	// USADO
	public List<Empleado> findAll();

	// USADO
	public List<Empleado> fetchByIdWithLocalWithEmpresa(Long id);

	// USADO
	public List<Empleado> fetchByIdWithLocalWithEmpresaNotAdmin(Long id);

	// USADO
	public List<Empleado> fetchByIdWithRoles();

	// USADO
	public Empleado findById(Long id) throws Exception;

	// USADO
	public Empleado findByUsername(String username);

	// USADO
	public Empleado findByUsernameAndLocal(String username, Long id_local);

	// USADO
	public Empleado findByRoleAndLocal(String role, Long id_local);

	// USADO
	public Empleado findByUsernameAndEstado(String username, boolean estado);

	// USADO
	public Empleado findByNroDocumento(String nroDocumento);

	// USADO
	public List<Empleado> findAllByNroDocumentoAndEstado(String term, boolean estado);

	// USADO
	public void save(Empleado empleado) throws Exception;

	// USADO
	public void update(Empleado empleado) throws Exception;

	// USADO
	public Empleado cambiarPassword(CambiarPassword form) throws Exception;

	// USADO
	public void isAdminListar(Model model, Principal principal);

	// USADO
	public void isAdminEditar(Map<String, Object> model, Principal principal);
}
