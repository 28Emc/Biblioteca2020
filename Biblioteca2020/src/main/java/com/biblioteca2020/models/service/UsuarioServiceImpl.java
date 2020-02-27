package com.biblioteca2020.models.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import com.biblioteca2020.models.dao.IUsuarioDao;
import com.biblioteca2020.models.entity.Usuario;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioDao usuarioDao;

	@Autowired
	private IRoleService roleService;

	/*
	 * MÉTODO PARA BUSCAR TODOS LOS USUARIOS POR SU NOMBRE Y ESTADO.
	 */
	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsernameAndEstado(String username, boolean estado) {
		return usuarioDao.findByUsernameAndEstado(username, estado);
	}

	/*
	 * MÉTODO PARA BUSCAR TODOS LOS USUARIOS.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Usuario> findAll() {
		return (List<Usuario>) usuarioDao.findAll();
	}

	/*
	 * MÉTODO PARA BUSCAR EL USUARIO POR SU ID.
	 */
	@Override
	@Transactional(readOnly = true)
	public Usuario findById(Long id) throws Exception {
		return usuarioDao.findById(id).orElseThrow(() -> new Exception("El usuario no existe."));
	}

	/*
	 * MÉTODO PARA BUSCAR EL USUARIO POR SU USERNAME.
	 */
	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsername(String username) {
		return usuarioDao.findByUsername(username);
	}

	/*
	 * MÉTODO PARA BUSCAR EL USUARIO POR SU NRO DOCUMENTO.
	 */
	@Override
	@Transactional(readOnly = true)
	public Usuario findByNroDocumento(String nroDocumento) {
		return usuarioDao.findByNroDocumento(nroDocumento);
	}

	/*
	 * MÉTODO PARA CREAR UN NUEVO USUARIO, CON VALIDACIONES.
	 */
	@Override
	@Transactional
	public void save(Usuario usuario) throws Exception {
		if (verificarUsername(usuario) && verificarPassword(usuario) && verificarDNI(usuario)) {
			usuario = usuarioDao.save(usuario);
		}
	}

	/*
	 * MÉTODO PARA ACTUALIZAR EL USUARIO.
	 */
	@Override
	@Transactional
	public void update(Usuario usuario) throws Exception {
		usuario = usuarioDao.save(usuario);
	}

	/*
	 * MÉTODO PARA ELIMINAR EL USUARIO.
	 */
	@Override
	@Transactional
	public void borrarUsuario(Long id) throws Exception {
		Usuario usuario = usuarioDao.findById(id)
				.orElseThrow(() -> new Exception("El usuario no existe en el método de borrar usuarios."));
		usuarioDao.deleteById(usuario.getId());
	}

	/*
	 * MÉTODO PARA LISTAR EL USUARIO CON SU ROL.
	 */
	@Override
	public List<Usuario> fetchByIdWithRoles() {
		return usuarioDao.fetchByIdWithRoles();
	}

	/*
	 * MÉTODO PARA VERIFICAR EL CAMPO USERNAME EN LA BD. EL CAMPO ES ÚNICO, Y
	 * CAPTURO SU EXCEPCION ESPECÍFICA PARA MOSTAR UN MENSJAE DE ERROR
	 * PERSONALIZADO.
	 */
	public boolean verificarUsername(Usuario usuario) throws ConstraintViolationException {
		Usuario usuarioEncontrado = usuarioDao.findByUsername(usuario.getUsername());
		if (usuarioEncontrado != null) {
			throw new ConstraintViolationException("El usuario ya está en uso.", null);
		}
		return true;
	}
	
	/*
	 * MÉTODO PARA VERIFICAR EL CAMPO CELULAR EN LA BD. EL CAMPO ES ÚNICO, Y
	 * CAPTURO SU EXCEPCION ESPECÍFICA PARA MOSTAR UN MENSJAE DE ERROR
	 * PERSONALIZADO.
	 */
	public boolean verificarCelular(Usuario usuario) throws ConstraintViolationException {
		Usuario usuarioEncontrado = usuarioDao.findByCelular(usuario.getCelular());
		if (usuarioEncontrado != null) {
			throw new ConstraintViolationException("El numero de celular ya está en uso.", null);
		}
		return true;
	}

	/*
	 * MÉTODO PARA VERIFICAR EL CAMPO DNI EN LA BD. EL CAMPO ES ÚNICO, Y CAPTURO SU
	 * EXCEPCION ESPECÍFICA PARA MOSTAR UN MENSJAE DE ERROR PERSONALIZADO.
	 */
	public boolean verificarDNI(Usuario usuario) throws ConstraintViolationException {
		Usuario usuarioEncontrado = usuarioDao.findByNroDocumento(usuario.getNroDocumento());
		if (usuarioEncontrado != null) {
			throw new ConstraintViolationException("El DNI ya está registrado con otro usuario.", null);
		}
		return true;
	}

	/*
	 * MÉTODO PARA VERIFICAR EL CAMPO CONTRASEÑA Y SU CONFIRMACIÓN.
	 */
	public boolean verificarPassword(Usuario user) throws Exception {
		if (user.getPasswordConfirmacion() == null || user.getPasswordConfirmacion().isEmpty()) {
			throw new Exception("Contraseñas obligatorias.");
		}

		if (!user.getPassword().equals(user.getPasswordConfirmacion())) {
			throw new Exception("Las contraseñas no coinciden.");
		}
		return true;
	}

	/*
	 * MÉTODO DONDE BUSCO EL USUARIO LOGUEADO POR SU USERNAME, VERIFICO SI EL
	 * SET<ROLE> DE ESTE USUARIO (CONVERTIDO EN CADENA) CONTIENE LA PALABRA
	 * "ROLE_ADMIN", SI ES ADMIN, MUESTRO TODOS LOS USUARIOS, SI NO LO ES, MUESTRO
	 * SOLO LOS EMPLEADOS.
	 */
	@Override
	public void isAdminListar(Model model, Principal principal) {
		Usuario user = findByUsername(principal.getName());
		Boolean isAdmin = user.getRoles().toString().contains("ROLE_ADMIN");
		model.addAttribute("usuario", new Usuario());
		if (isAdmin) {
			model.addAttribute("usuarios", findAll());
		} else {
			model.addAttribute("usuarios", fetchByIdWithRoles());
		}
	}

	/*
	 * MÉTODO SIMILAR AL ANTERIOR, CON LA DIFERENCIA QUE MUESTRO EL ROL.
	 */
	@Override
	public void isAdminEditar(Map<String, Object> model, Principal principal) {
		Usuario user = findByUsername(principal.getName());
		Boolean isAdmin = user.getRoles().toString().contains("ROLE_ADMIN");
		if (isAdmin) {
			model.put("roles", roleService.findAll());
		} else {
			model.put("roles", roleService.findNotAdmins());
		}
	}

}
