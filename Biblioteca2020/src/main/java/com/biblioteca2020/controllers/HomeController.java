package com.biblioteca2020.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IUsuarioService;

@Controller
public class HomeController {

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IUsuarioService usuarioService;

	// ESTA PÁGINA ME SIRVE SOLO PARA MOSTRAR UNA PÁGINA EN COMÚN AL INGRESAR PARA
	// TODOS LOS USUARIOS;
	// A PARTIR DE AQUÍ, YO CON SPRING SECURITY FILTRO MEDIANTE ROLES LOS ACCESOS
	@GetMapping({ "/home", "/" })
	public String home(Model model, Authentication authentication) {
		model.addAttribute("titulo", "Home");
		// CON EL OBJETO USERDETAILS ENCUENTRO LA DATA DEL USUARIO LOGUEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("rol", userDetails.getAuthorities().toString());

		Empleado empleado = null;
		empleado = empleadoService.findByUsername(userDetails.getUsername().toString());
		model.addAttribute("empleado", empleado);

		Usuario usuario = null;
		usuario = usuarioService.findByUsername(userDetails.getUsername().toString());
		model.addAttribute("usuario", usuario);
		return "home";
	}
}
