package com.biblioteca2020.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	// ESTA PÁGINA ME SIRVE SOLO PARA MOSTRAR UNA PÁGINA EN COMÚN AL INGRESAR PARA TODOS LOS USUARIOS;
	// A PARTIR DE AQUÍ, YO CON SPRING SECURITY FILTRO MEDIANTE ROLES LOS ACCESOS 
	@GetMapping({"/home", "/"})
	public String home(Model model) {
		model.addAttribute("titulo", "Home");
		return "home";
	}
}
