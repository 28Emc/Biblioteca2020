package com.biblioteca2020.models.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import com.biblioteca2020.models.dao.IEmpleadoDao;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Empleado;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

	@Autowired
	private IEmpleadoDao empleadoDao;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByUsernameAndEstado(String username, boolean estado) {
		return empleadoDao.findByUsernameAndEstado(username, estado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> findAll() {
		return (List<Empleado>) empleadoDao.findAll();
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findById(Long id) throws Exception {
		return empleadoDao.findById(id).orElseThrow(() -> new Exception("El empleado no existe."));
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByUsername(String username) {
		return empleadoDao.findByUsername(username);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> findAllByNroDocumentoAndEstado(String term, boolean estado) {
		return empleadoDao.findAllByNroDocumentoAndEstado("%" + term + "%", estado);
	}

	// USADO
	@Override
	@Transactional
	public void save(Empleado empleado) throws Exception {
		if (verificarUsername(empleado) && verificarPassword(empleado) && verificarDNI(empleado)) {
			String passwordHash = passwordEncoder.encode(empleado.getPassword());
			empleado.setPassword(passwordHash);
			empleadoDao.save(empleado);
		}
	}

	// USADO
	@Override
	@Transactional
	public void update(Empleado empleado) throws Exception {
		empleadoDao.save(empleado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> fetchByIdWithRoles(String term) {
		return empleadoDao.fetchByIdWithRoles("%" + term + "%");
	}

	// USADO
	public boolean verificarUsername(Empleado empleado) throws ConstraintViolationException {
		Empleado empleadoEncontrado = empleadoDao.findByUsername(empleado.getUsername());
		if (empleadoEncontrado != null) {
			throw new ConstraintViolationException("El usuario ya está en uso.", null);
		}
		return true;
	}

	/*
	 * public boolean verificarCelular(Empleado empleado) throws
	 * ConstraintViolationException { Empleado empleadoEncontrado =
	 * empleadoDao.findByCelular(empleado.getCelular()); if (empleadoEncontrado !=
	 * null) { throw new
	 * ConstraintViolationException("El numero de celular ya está en uso.", null); }
	 * return true; }
	 */

	// USADO
	public boolean verificarDNI(Empleado empleado) throws ConstraintViolationException {
		Empleado empleadoEncontrado = empleadoDao.findByNroDocumento(empleado.getNroDocumento());
		if (empleadoEncontrado != null) {
			throw new ConstraintViolationException("El DNI ya está registrado con otro usuario.", null);
		}
		return true;
	}

	// USADO
	public boolean verificarPassword(Empleado empleado) throws Exception {
		if (empleado.getPasswordConfirmacion() == null || empleado.getPasswordConfirmacion().isEmpty()) {
			throw new Exception("Contraseñas obligatorias.");
		}

		if (!empleado.getPassword().equals(empleado.getPasswordConfirmacion())) {
			throw new Exception("Las contraseñas no coinciden.");
		}
		return true;
	}

	// USADO
	@Override
	public void isAdminListar(Model model, Principal principal) {
		Empleado empleado = findByUsername(principal.getName());
		Boolean isAdmin = empleado.getRoles().toString().contains("ROLE_EMPLEADO");
		model.addAttribute("empleado", new Empleado());
		if (isAdmin) {
			model.addAttribute("empleado", findAll());
		} else {
			model.addAttribute("empleado", fetchByIdWithRoles());
		}
	}

	// USADO
	@Override
	public void isAdminEditar(Map<String, Object> model, Principal principal) {
		Empleado empleado = findByUsername(principal.getName());
		Boolean isAdmin = empleado.getRoles().toString().contains("ROLE_EMPLEADO");
		if (isAdmin) {
			model.put("roles", roleService.findForEmpleadosAndAdmin());
		} else {
			model.put("roles", roleService.findOnlyUsers());
		}
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> fetchByIdWithLocalWithEmpresaNotAdmin(Long id) {
		return empleadoDao.fetchByIdWithLocalWithEmpresaNotAdmin(id);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> fetchByIdWithLocalWithEmpresa(Long id) {
		return empleadoDao.fetchByIdWithLocalWithEmpresa(id);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByNroDocumento(String nroDocumento) {
		return empleadoDao.findByNroDocumento(nroDocumento);
	}

	// USADO
	@Override
	public Empleado cambiarPassword(CambiarPassword form) throws Exception {
		Empleado empleado = findById(form.getId());

		if (!passwordEncoder.matches(form.getPasswordActual(), empleado.getPassword())) {
			throw new Exception("La contraseña actual es incorrecta");
		}

		if (passwordEncoder.matches(form.getNuevaPassword(), empleado.getPassword())) {
			throw new Exception("La nueva contraseña debe ser diferente a la actual");
		}

		if (!form.getNuevaPassword().equals(form.getConfirmarPassword())) {
			throw new Exception("Las contraseñas no coinciden");
		}
		String passwordHash = passwordEncoder.encode(form.getNuevaPassword());
		empleado.setPassword(passwordHash);
		return empleadoDao.save(empleado);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByUsernameAndLocal(String username, Long id_local) {
		return empleadoDao.findByUsernameAndLocal("%" + username + "%", id_local);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByRoleAndLocal(String role, Long id_local) {
		return empleadoDao.findByRoleAndLocal("%" + role + "%", id_local);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> fetchByIdWithRolesSysAdmin(String term) {
		return empleadoDao.fetchByIdWithRolesSysAdmin("%" + term + "%");
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public Empleado findByRoleAndLocalNotAdmin(Long id_local) {
		return empleadoDao.findByRoleAndLocalNotAdmin(id_local);
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> fetchByIdWithRoles() {
		return empleadoDao.fetchByIdWithRoles();
	}

	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Empleado> findByLocal(Long id_local) {
		return empleadoDao.findByLocal(id_local);
	}
}
