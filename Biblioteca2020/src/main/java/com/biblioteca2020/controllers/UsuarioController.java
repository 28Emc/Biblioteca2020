package com.biblioteca2020.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.IRoleService;
import com.biblioteca2020.models.service.IUsuarioService;

@Controller
@RequestMapping("/usuarios")
@SessionAttributes("usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IRoleService roleService;

	@GetMapping("/listar")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuarios", usuarioService.findAll());
		model.addAttribute("titulo", "Listado de Usuarios");
		return "usuarios/listar";
	}

	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap) {
		return "redirect:/usuarios/listar";
	}

	@GetMapping("/crear")
	public String crearFormUsuario(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findAll());
		model.addAttribute("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PostMapping("/crear")
	public String crearUsuario(@Valid @ModelAttribute("usuario")Usuario usuario, BindingResult result, ModelMap model,
			RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/crear";
		} else {
			try {
				usuarioService.save(usuario);
				flash.addFlashAttribute("success",
						"El usuario '" + usuario.getUsername() + "' ha sido registrado en la base de datos.");
				status.setComplete();
				return "redirect:/usuarios/listar";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findAll());
				model.addAttribute("titulo", "Registro de Usuario");
				return "/usuarios/crear";
			}
		}
	}

	@GetMapping("/editar/{id}")
	public String editarFormUsuario(Model model, @PathVariable(name = "id")Long id) throws Exception {
		Usuario usuarioEditable = usuarioService.findById(id);
		model.addAttribute("usuario", usuarioEditable);
		model.addAttribute("roles", roleService.findAll());
		model.addAttribute("titulo", "Modificar Usuario");
		model.addAttribute("editable", "true");
		return "/usuarios/crear";
	}

	// NO EDITA LOS CAMPOS
	@PostMapping("/editar")
	public String editarUsuario(@Valid @ModelAttribute("usuario")Usuario usuario, BindingResult result, ModelMap model,
			RedirectAttributes flash, SessionStatus status) throws Exception {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Modificar Usuario");
			model.addAttribute("editable", "true");
			return "/usuarios/crear";
		} else {
			try {
				usuarioService.update(usuario);
				flash.addFlashAttribute("success",
						"Los datos del usuario '" + usuario.getUsername() + "' han sido actualizados.");
				model.addAttribute("editable", "false");
				status.setComplete();
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				System.out.println(e.getMessage());
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findAll());
				model.addAttribute("titulo", "Modificar Usuario");
				model.addAttribute("editable", "true");
				return "/usuarios/crear";
			}
		}
		return "redirect:/usuarios/listar";
	}
}
