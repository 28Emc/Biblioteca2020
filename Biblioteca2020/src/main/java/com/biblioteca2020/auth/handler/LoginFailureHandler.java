package com.biblioteca2020.auth.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		/* EXPERIMENTOS */
		HttpSession session = request.getSession();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!authUser.isEnabled()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
			FlashMap flashMap = new FlashMap();
			flashMap.put("error",
					"Lo sentimos, su usuario ha sido bloqueado. Contace al administrador para mayor detalles.");
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
			response.sendRedirect("/login");
		} else {
			session.setAttribute("username", authUser.getUsername());
			session.setAttribute("password", authUser.getPassword());
			session.setAttribute("estado", authUser.isEnabled());
			response.setStatus(HttpServletResponse.SC_OK);
			/* FIN EXPERIMENTOS */

			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
