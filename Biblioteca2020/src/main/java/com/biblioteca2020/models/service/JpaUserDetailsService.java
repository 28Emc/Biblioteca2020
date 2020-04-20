package com.biblioteca2020.models.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IUsuarioDao;
import com.biblioteca2020.models.entity.Role;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.entity.UsuarioLog;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioDao usuarioDao;

	@Autowired
	private IUsuarioLogService usuarioLogService;

	private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);

	@Override
	// HAGO MI TRANSACCION NO SOLAMENTE DE LECTURA
	// YA QUE REALIZO UN INSERT A MI TABLA LOG
	// @Transactional(readOnly = true)
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
		Usuario usuario = usuarioDao.findByUsername(username);

		if (usuario == null) {
			logger.error("No existe el usuario '" + username + "'");
			throw new UsernameNotFoundException("El usuario '" + username + "' no existe en el sistema.");
		}
		// LLAMO A LA EXCEPCIÓN DE USUARIO DESHABILITADO (DisabledException) PARA MANDAR
		// UN ERROR A LA VISTA DEL LOGIN
		if (usuario.getEstado() == false) {
			logger.error("Error: Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada.");
			throw new DisabledException("Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada.");
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		for (Role role : usuario.getRoles()) {
			logger.info("Rol: '".concat(role.getAuthority()));
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}

		if (authorities.isEmpty()) {
			logger.error("El usuario '" + username + "' no tiene roles asignados");
			throw new UsernameNotFoundException("Username " + username + " no tiene roles asignados.");
		}

		// AL INICIAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
		Long idRole = usuario.getRoles().iterator().next().getId();
		usuarioLogService.save(new UsuarioLog(idRole, usuario.getNombres(), null, usuario.getApellidos(), null,
				usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
				usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
				usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null, "LOGIN USER",
				new Date(), null, null));

		return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEstado(), true, true, true,
				authorities);
	}

}
