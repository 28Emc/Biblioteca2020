package com.biblioteca2020.controllers;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

	// ESTE METODO SOLO SIRVE PARA VALIDAR EL LOGIN, NO EFECTUA LA PETICION POST DEL
	// LOGUEO
	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model, Principal principal,
			RedirectAttributes flash) {
		if (principal != null) {
			// SOLAMENTE VALIDO QUE YA INICIÉ SESIÓN
			flash.addFlashAttribute("info", "Ya has iniciado sesión, " + principal.getName());
			return "redirect:/home";
		}

		// MENSAJE DE LOGOUT
		if (logout != null) {
			model.addAttribute("success", "Ha cerrado sesiòn con éxito");
		}

		model.addAttribute("titulo", "Login");
		return "login";
	}
}
