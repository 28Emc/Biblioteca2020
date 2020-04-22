package com.biblioteca2020.auth.handler;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.EmpleadoLog;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.entity.UsuarioLog;
import com.biblioteca2020.models.service.IEmpleadoLogService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IUsuarioLogService;
import com.biblioteca2020.models.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IEmpleadoLogService empleadoLogService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IUsuarioLogService usuarioLogService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
		FlashMap flashMap = new FlashMap();
		// MOSTRAR UN MENSAJE DE BIENVENIDA AL AUTENTICARME
		flashMap.put("success", "Hola " + authentication.getName() + ", ha iniciado sesiòn con éxito");
		flashMapManager.saveOutputFlashMap(flashMap, request, response);
		// AL INICIAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE EMPLEADOS O USUARIOS
		// DEPENDIENDO DE SU ROL
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String authority = userDetails.getAuthorities().toString();
		switch (authority) {
			case "[ROLE_ADMIN]":
				Empleado empleadoAdmin = empleadoService.findByUsername(userDetails.getUsername());
				Long idRoleAdmin = empleadoAdmin.getRoles().iterator().next().getId();
				empleadoLogService.save(new EmpleadoLog(idRoleAdmin, empleadoAdmin.getNombres(), null,
						empleadoAdmin.getApellidos(), null, empleadoAdmin.getNroDocumento(), null,
						empleadoAdmin.getDireccion(), null, empleadoAdmin.getEmail(), null, empleadoAdmin.getCelular(),
						null, empleadoAdmin.getFecha_registro(), null, empleadoAdmin.getUsername(), null,
						empleadoAdmin.getPassword(), null, empleadoAdmin.getEstado(), null,
						empleadoAdmin.getFoto_empleado(), null, "LOGIN BY ADMIN", new Date(), null, null));
				break;
			case "[ROLE_EMPLEADO]":
				Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
				Long idRoleEmpleado = empleado.getRoles().iterator().next().getId();
				empleadoLogService.save(new EmpleadoLog(idRoleEmpleado, empleado.getNombres(), null,
						empleado.getApellidos(), null, empleado.getNroDocumento(), null, empleado.getDireccion(), null,
						empleado.getEmail(), null, empleado.getCelular(), null, empleado.getFecha_registro(), null,
						empleado.getUsername(), null, empleado.getPassword(), null, empleado.getEstado(), null,
						empleado.getFoto_empleado(), null, "LOGIN BY EMPLOYEE", new Date(), null, null));
				break;
			case "[ROLE_USER]":
				Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
				Long idRoleUsuario = usuario.getRoles().iterator().next().getId();
				usuarioLogService.save(new UsuarioLog(idRoleUsuario, usuario.getNombres(), null, usuario.getApellidos(),
						null, usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
						usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
						usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null,
						"LOGIN BY USER", new Date(), null, null));
				break;
		}

		if (authentication != null) {
			logger.info("El usuario " + authentication.getName() + " ha iniciado sesión con éxito.");
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
