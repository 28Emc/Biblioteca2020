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
import com.biblioteca2020.models.dao.IEmpleadoDao;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.EmpleadoLog;
import com.biblioteca2020.models.entity.Role;

@Service("jpaEmpleadorDetailsService")
public class JpaEmpleadoDetailsService implements UserDetailsService {

	@Autowired
	private IEmpleadoDao empleadoDao;

	@Autowired
	private IEmpleadoLogService empleadoLogService;

	private Logger logger = LoggerFactory.getLogger(JpaEmpleadoDetailsService.class);

	@Override
	//@Transactional(readOnly = true)
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
		Empleado empleado = empleadoDao.findByUsername(username);

		if (empleado == null) {
			logger.error("No existe el empleado '" + username + "'");
			throw new UsernameNotFoundException("Username " + username + " no existe en el sistema.");
		}
		// LLAMO A LA EXCEPCIÓN DE USUARIO DESHABILITADO (DisabledException) PARA MANDAR
		// UN ERROR A LA VISTA DEL LOGIN
		if (empleado.getEstado() == false) {
			logger.error("Error: Lo sentimos, '" + username
					+ "', su cuenta ha sido deshabilitada. Contacte con el administrador para más detalles.");
			throw new DisabledException("Lo sentimos, '" + username
					+ "', su cuenta ha sido deshabilitada. Contacte con el administrador para más detalles.");
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

		// AL INICIAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE EMPLEADOS
		Long idRoleEmpleado = empleado.getRoles().iterator().next().getId();
		// VALIDO SI ES ADMIN O EMPLEADO
		String authority = empleado.getRoles().iterator().next().getAuthority();
		switch (authority) {
			case "ROLE_ADMIN":
				empleadoLogService.save(new EmpleadoLog(idRoleEmpleado, empleado.getNombres(), null,
						empleado.getApellidos(), null, empleado.getNroDocumento(), null, empleado.getDireccion(), null,
						empleado.getEmail(), null, empleado.getCelular(), null, empleado.getFecha_registro(), null,
						empleado.getUsername(), null, empleado.getPassword(), null, empleado.getEstado(), null,
						empleado.getFoto_empleado(), null, "LOGIN BY ADMIN", new Date(), null, null));
				break;
			case "ROLE_EMPLEADO":
				empleadoLogService.save(new EmpleadoLog(idRoleEmpleado, empleado.getNombres(), null,
						empleado.getApellidos(), null, empleado.getNroDocumento(), null, empleado.getDireccion(), null,
						empleado.getEmail(), null, empleado.getCelular(), null, empleado.getFecha_registro(), null,
						empleado.getUsername(), null, empleado.getPassword(), null, empleado.getEstado(), null,
						empleado.getFoto_empleado(), null, "LOGIN BY EMPLOYEE", new Date(), null, null));
				break;
		}

		return new User(empleado.getUsername(), empleado.getPassword(), empleado.getEstado(), true, true, true,
				authorities);
	}

}
