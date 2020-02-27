package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IUsuarioDao;
import com.biblioteca2020.models.entity.Usuario;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioDao usuarioDao;

	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsernameAndEstado(String username, boolean estado) {
		return usuarioDao.findByUsernameAndEstado(username, estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> findAll() {
		return (List<Usuario>) usuarioDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findById(Long id) throws Exception {
		return usuarioDao.findById(id).orElseThrow(() -> new Exception("El usuario no existe."));
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsername(String username) {
		return usuarioDao.findByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findByNroDocumento(String nroDocumento) {
		return usuarioDao.findByNroDocumento(nroDocumento);
	}

	@Override
	@Transactional
	public void saveNew(Usuario usuario) throws Exception {
		if (verificarUsuario(usuario) && verificarPassword(usuario) && verificarDNI(usuario)) {
			usuario = usuarioDao.save(usuario);
		}
	}

	@Override
	@Transactional
	public void save(Usuario usuario) throws Exception {
		usuarioDao.save(usuario);
	}

	/*
	 * @Override
	 * 
	 * @Transactional public void saveUsuario(Usuario usuario) throws Exception {
	 * usuarioDao.save(usuario); }
	 */

	/*
	 * @Override //@Transactional public Usuario update(Usuario usuarioEntrada)
	 * throws Exception { Usuario usuarioSalida = findById(usuarioEntrada.getId());
	 * mapUser(usuarioEntrada, usuarioEntrada); return
	 * usuarioDao.save(usuarioSalida); }
	 */

	/*
	 * @Override
	 * 
	 * @Transactional public void deshabilitar(Long id) throws Exception { Usuario
	 * usuario = usuarioDao.findById(id) .orElseThrow(() -> new
	 * Exception("El usuario no existe en el método de borrar usuarios."));
	 * usuario.setEstado(false); usuarioDao.save(usuario); }
	 */

	@Override
	@Transactional
	public void borrarUsuario(Long id) throws Exception {
		Usuario usuario = usuarioDao.findById(id)
				.orElseThrow(() -> new Exception("El usuario no existe en el método de borrar usuarios."));
		usuarioDao.deleteById(usuario.getId());
	}

	public boolean verificarUsuario(Usuario usuario) throws Exception {
		Usuario usuarioEncontrado = usuarioDao.findByUsername(usuario.getUsername());
		if (usuarioEncontrado != null) {
			throw new Exception("El usuario no esta disponible.");
		}
		return true;
	}

	public boolean verificarDNI(Usuario usuario) throws Exception {
		Usuario usuarioEncontrado = usuarioDao.findByNroDocumento(usuario.getNroDocumento());
		if (usuarioEncontrado != null) {
			throw new Exception("El DNI ya está registrado con otro usuario.");
		}
		return true;
	}

	public boolean verificarPassword(Usuario user) throws Exception {
		if (user.getPasswordConfirmacion() == null || user.getPasswordConfirmacion().isEmpty()) {
			throw new Exception("Contraseñas obligatorias.");
		}

		if (!user.getPassword().equals(user.getPasswordConfirmacion())) {
			throw new Exception("Las contraseñas no coinciden.");
		}
		return true;
	}

	protected void mapUser(Usuario from, Usuario to) {
		to.setRoles(from.getRoles());
		to.setNombres(from.getNombres());
		to.setApellidos(from.getApellidos());
		to.setNroDocumento(from.getNroDocumento());
		to.setDireccion(from.getDireccion());
		to.setEmail(from.getEmail());
		to.setCelular(from.getCelular());
		to.setUsername(from.getUsername());
	}

	@Override
	public List<Usuario> fetchByIdWithRoles() {
		return usuarioDao.fetchByIdWithRoles();
	}

}
