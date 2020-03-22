package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.ILibroService;
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

	@Autowired
	private ILibroService libroService;

	// LISTADO DE USUARIOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping("/listar")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("usuarios", usuarioService.findAll());
		model.addAttribute("titulo", "Listado de Usuarios");
		return "usuarios/listar";
	}

	// CATÁLOGO DE LIBROS PARA EL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_SUPERVISOR', 'ROLE_USER')")
	@GetMapping(value = "/librosAllUser")
	public String listarAllLibrosUser(Model model, Principal principal) {
		model.addAttribute("titulo", "Catálogo de libros");
		List<Libro> libros = libroService.findAll();
		
		// LÓGICA DE MOSTRAR UNA DESCRICIÓN REDUCIDA DE 150 CARACTERES DEL LIBRO
		for (int i = 0; i < libros.size(); i++) {
			String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
			String descripcionFull = libros.get(i).getDescripcion().substring(150,
					libros.get(i).getDescripcion().length());
			libros.get(i).setDescripcionMin(descripcionMin + " ...");
			libros.get(i).setDescripcion(descripcionFull);
			model.addAttribute("libros", libros);
		}

		// model.addAttribute("libros", libros);
		return "/usuarios/librosAllUser";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/crear")
	public String crearFormUsuario(Map<String, Object> modelMap) {
		modelMap.put("usuario", new Usuario());
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/crear")
	public String crearUsuario(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/crear";
		}

		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				usuario.setFoto_usuario(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}

		try {
			usuarioService.save(usuario);
			flash.addFlashAttribute("success",
					"El usuario ha sido registrado en la base de datos (Código " + usuario.getId() + ")");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/editar/{id}")
	public String editarFormUsuario(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			RedirectAttributes flash) {
		Usuario usuario = null;
		modelMap.put("editable", true);
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Modificar Usuario");
		try {
			usuario = usuarioService.findById(id);
			modelMap.put("usuario", usuario);
			return "/usuarios/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/editar")
	public String guardarUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Usuario");
			return "/usuarios/editar";
		}

		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				usuario.setFoto_usuario(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}

		try {
			usuarioService.update(usuario);
			flash.addFlashAttribute("warning",
					"El usuario con código " + usuario.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Usuario usuario = null;
		try {
			usuario = usuarioService.findById(id);
			usuario.setEstado(false);
			usuarioService.update(usuario);
			flash.addFlashAttribute("info", "El usuario con código " + usuario.getId() + " ha sido deshabilitado.");
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}
}
