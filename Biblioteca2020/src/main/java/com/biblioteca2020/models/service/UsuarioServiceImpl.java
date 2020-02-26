package com.biblioteca2020.models.service;

import java.util.List;
import java.util.Optional;
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
		return usuarioDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> findByUsername(String username) {
		return usuarioDao.findByUsername(username);
	}

	@Override
	public Optional<Usuario> findByNroDocumento(String nroDocumento) {
		return usuarioDao.findByNroDocumento(nroDocumento);
	}

	public boolean verificarUsuario(Usuario usuario) throws Exception {
		Optional<Usuario> usuarioEncontrado = usuarioDao.findByUsername(usuario.getUsername());
		if (usuarioEncontrado.isPresent()) {
			throw new Exception("El usuario no esta disponible.");
		}

		Optional<Usuario> usuarioEncontrado2 = usuarioDao.findByNroDocumento(usuario.getNroDocumento());
		if (usuarioEncontrado2.isPresent()) {
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

	@Override
	public Usuario save(Usuario usuario) throws Exception {
		if (verificarUsuario(usuario) && verificarPassword(usuario)) {
			usuario = usuarioDao.save(usuario);
		}
		return usuario;
	}

	@Override
	public Usuario findById(Long id) throws Exception {
		return usuarioDao.findById(id).orElseThrow(() -> new Exception("El usuario no existe."));
	}

	@Override
	public Usuario update(Usuario usuarioEntrada) throws Exception {
		Usuario usuarioSalida = findById(usuarioEntrada.getId());
		mapUser(usuarioEntrada, usuarioEntrada);
		return usuarioDao.save(usuarioSalida);
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
		/*to.setEstado(true);
		to.setFecha_registro(new Date());*/
	}

}
