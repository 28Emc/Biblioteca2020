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
import com.biblioteca2020.models.dao.IEmpleadoDao;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Role;

@Service("jpaEmpleadorDetailsService")
public class JpaEmpleadoDetailsService implements UserDetailsService {

	@Autowired
	private IEmpleadoDao empleadoDao;

	private Logger logger = LoggerFactory.getLogger(JpaEmpleadoDetailsService.class);

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
		Empleado empleado = empleadoDao.findByUsername(username);

		if (empleado == null) {
			logger.error("No existe el empleado '" + username + "'");
			throw new UsernameNotFoundException("Username " + username + " no existe en el sistema.");
		}
		// LLAMO A LA EXCEPCIÓN DE USUARIO DESHABILITADO (DisabledException) PARA MANDAR UN ERROR A LA VISTA DEL LOGIN
		if (empleado.getEstado() == false) {
			logger.error("Error: Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada. Contacte con el administrador para más detalles.");
			throw new DisabledException("Lo sentimos, '" + username + "', su cuenta ha sido deshabilitada. Contacte con el administrador para más detalles.");
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		for (Role role : empleado.getRoles()) {
			logger.info("Rol: '".concat(role.getAuthority()));
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}

		if (authorities.isEmpty()) {
			logger.error("El empleado '" + username + "' no tiene roles asignados");
			throw new UsernameNotFoundException("Username " + username + " no tiene roles asignados.");
		}

		return new User(empleado.getUsername(), empleado.getPassword(), empleado.getEstado(), true, true, true,
				authorities);
	}

}
