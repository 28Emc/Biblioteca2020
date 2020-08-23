package com.biblioteca2020.models.service;

import java.util.ArrayList;
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

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioDao usuarioDao;

	private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
		
		Usuario usuario = usuarioDao.findByUsername(username);
		if (usuario == null) {
			logger.error("No existe el usuario '" + username + "'");
			throw new UsernameNotFoundException("El usuario '" + username + "' no existe en el sistema.");
		} else if (usuario.getEstado() == false) {
			logger.error("Error: Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada.");
			// LLAMO A LA EXCEPCIÓN DE USUARIO DESHABILITADO (DisabledException) PARA MANDAR
			// UN ERROR A LA VISTA DEL LOGIN
			throw new DisabledException("Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada.");
		} else {
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			for (Role role : usuario.getRoles()) {
				logger.info("Rol: '".concat(role.getAuthority()));
				authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
			}
			if (authorities.isEmpty()) {
				logger.error("El usuario '" + username + "' no tiene roles asignados");
				throw new UsernameNotFoundException("Username " + username + " no tiene roles asignados.");
			}
			return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEstado(), true, true, true,
					authorities);
		}
	}

}
