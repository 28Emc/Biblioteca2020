package com.biblioteca2020.controllers;

import java.security.Principal;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/listar")
	public String listarUsuarios(Model model, Principal principal) {
		// AQUÍ BUSCO EL USUARIO LOGUEADO POR SU USERNAME
		Usuario user = usuarioService.findByUsername(principal.getName());
		// AQUÍ VERIFICO SI EL SET<ROLE> DE ESTE USUARIO (CONVERTIDO EN CADENA)
		// CONTIENE LA PALABRA "ROLE_ADMIN"
		Boolean isAdmin = user.getRoles().toString().contains("ROLE_ADMIN");
		// System.out.println(isAdmin);
		model.addAttribute("usuario", new Usuario());
		// SI ES ADMIN, MUESTRO TODOS LOS USUARIOS ...
		if (isAdmin) {
			model.addAttribute("usuarios", usuarioService.findAll());
		} else {
			// ... SI NO LO ES, MUESTRO SOLO LOS EMPLEADOS (EL USUARIO COMÚN NO PUEDE ENTRAR
			// A ESTE MÉTODO)
			model.addAttribute("usuarios", usuarioService.fetchByIdWithRoles());
		}
		model.addAttribute("titulo", "Listado de Usuarios");
		return "usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap) {
		return "redirect:/usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/crear")
	public String crearFormUsuario(Map<String, Object> model) {
		model.put("usuario", new Usuario());
		model.put("roles", roleService.findAll());
		model.put("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@PostMapping(value = "/crear")
	public String crearUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/crear";
		}
		try {
			usuarioService.saveNew(usuario);
			flash.addFlashAttribute("success",
					"El usuario ha sido registrado en la base de datos (Código " + usuario.getId() + ")");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Registro de Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/editar/{id}")
	public String editarFormUsuario(@PathVariable(value = "id") Long id, Map<String, Object> model,
			RedirectAttributes flash) {
		Usuario usuario = null;
		model.put("editable", true);
		model.put("roles", roleService.findAll());
		model.put("titulo", "Modificar Usuario");
		try {
			usuario = usuarioService.findById(id);
			model.put("usuario", usuario);
			return "/usuarios/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@PostMapping(value = "/editar")
	public String guardarUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Modificar Usuario");
			return "/usuarios/editar";
		}
		try {
			usuarioService.save(usuario);
			flash.addFlashAttribute("success",
					"El usuario con código " + usuario.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("titulo", "Modificar Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Usuario usuario = null;
		try {
			usuario = usuarioService.findById(id);
			usuario.setEstado(false);
			usuarioService.save(usuario);
			flash.addFlashAttribute("warning", "El usuario con código " + usuario.getId() + " ha sido deshabilitado.");
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			System.out.println(e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/eliminar/{id}")
	public String eliminarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Usuario usuario = null;
		try {
			usuario = usuarioService.findById(id);
			flash.addFlashAttribute("error",
					"El usuario con código " + usuario.getId() + " ha sido eliminado de la base de datos.");
			usuarioService.borrarUsuario(id);
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			System.out.println(e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}

}
