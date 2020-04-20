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
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Autowired
    private IUsuarioLogService usuarioLogService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IEmpleadoLogService empleadoLogService;

    @Autowired
    private IEmpleadoService empleadoService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
        FlashMap flashMap = new FlashMap();
        flashMap.put("success", "Ha cerrado sesiòn con éxito");
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
        Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);

        // VALIDO SI EL QUE REALIZÓ EL LOGOUT FUE UN USUARIO O EMPLEADO
        if (usuario != null) {
            // AL CERRAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
            Long idRoleUsuario = usuario.getRoles().iterator().next().getId();
            usuarioLogService.save(new UsuarioLog(idRoleUsuario, usuario.getNombres(), null, usuario.getApellidos(),
                    null, usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
                    usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
                    usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null,
                    "LOGOUT BY USER", new Date(), null, null));
        } else if (empleado != null) {
            // AL CERRAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE EMPLEADOS
            Long idRoleEmpleado = empleado.getRoles().iterator().next().getId();
            // VALIDO SI ES ADMIN O EMPLEADO
            String authority = empleado.getRoles().iterator().next().getAuthority();
            switch (authority) {
                case "ROLE_ADMIN":
                    empleadoLogService.save(new EmpleadoLog(idRoleEmpleado, empleado.getNombres(), null,
                            empleado.getApellidos(), null, empleado.getNroDocumento(), null, empleado.getDireccion(),
                            null, empleado.getEmail(), null, empleado.getCelular(), null, empleado.getFecha_registro(),
                            null, empleado.getUsername(), null, empleado.getPassword(), null, empleado.getEstado(),
                            null, empleado.getFoto_empleado(), null, "LOGOUT BY ADMIN", new Date(), null, null));
                    break;
                case "ROLE_EMPLEADO":
                    empleadoLogService.save(new EmpleadoLog(idRoleEmpleado, empleado.getNombres(), null,
                            empleado.getApellidos(), null, empleado.getNroDocumento(), null, empleado.getDireccion(),
                            null, empleado.getEmail(), null, empleado.getCelular(), null, empleado.getFecha_registro(),
                            null, empleado.getUsername(), null, empleado.getPassword(), null, empleado.getEstado(),
                            null, empleado.getFoto_empleado(), null, "LOGOUT BY EMPLOYEE", new Date(), null, null));
                    break;
            }
        }

        super.onLogoutSuccess(request, response, authentication);
    }

}