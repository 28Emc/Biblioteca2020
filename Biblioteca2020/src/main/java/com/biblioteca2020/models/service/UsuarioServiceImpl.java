package com.biblioteca2020.models.service;

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IUsuarioDao;
import com.biblioteca2020.models.entity.Usuario;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioDao usuarioDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

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
			String passwordHash = passwordEncoder.encode(usuario.getPassword());
			usuario.setPassword(passwordHash);
			usuarioDao.save(usuario);
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

	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsernameAndEstado(String username, boolean estado) {
		return usuarioDao.findByUsernameAndEstado(username, estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> findAllByNroDocumento(String term) {
		return usuarioDao.findAllByNroDocumento("%" + term + "%");
	}
}
