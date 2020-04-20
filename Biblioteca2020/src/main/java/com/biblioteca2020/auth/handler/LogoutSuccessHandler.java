package com.biblioteca2020.auth.handler;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.entity.UsuarioLog;
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

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
        FlashMap flashMap = new FlashMap();
        flashMap.put("success", "Ha cerrado sesiòn con éxito");
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);

        // AL CERRAR SESIÓN, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
        Long idRole = usuario.getRoles().iterator().next().getId();
        usuarioLogService.save(new UsuarioLog(idRole, usuario.getNombres(), null, usuario.getApellidos(), null,
                usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
                usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
                usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null, "LOGOUT USER",
                new Date(), null, null));

        super.onLogoutSuccess(request, response, authentication);
    }

}