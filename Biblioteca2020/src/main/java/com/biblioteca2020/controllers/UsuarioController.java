package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca2020.models.dao.IConfirmationTokenDao;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.ConfirmationToken;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.EmailSenderService;
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
	private IConfirmationTokenDao confirmationTokenRepository;

	@Autowired
	private EmailSenderService emailSenderService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private ILibroService libroService;

	// ----------------------------- ROLE USER

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

		return "/usuarios/librosAllUser";
	}

	@GetMapping("/crearPerfil")
	public String perfil(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de Usuario");
		return "/usuarios/perfil";
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cancelarPerfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	@PostMapping(value = "/crearPerfil")
	public String crearPerfil(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/perfil";
		}

		/* VALIDACIÓN EMAIL */
		Usuario usuarioExistente = usuarioService.findByEmailIgnoreCase(usuario.getEmail());
		if (usuarioExistente != null) {
			model.addAttribute("error", "El correo ya está asociado a otro usuario!");
			return "/usuarios/perfil";
		} else {
			/* LÓGICA DE REGISTRO DE USUARIOS */
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
				
				/* REGISTRO EL TOKEN DE REGISTRO SEGUN EL CORREO DEL USUARIO, PARA SU VALIDACIÒN */ 
				ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
				confirmationTokenRepository.save(confirmationToken);
				
				/* ENVÌO DEL CORREO DE VALIDACIÒN */
				SimpleMailMessage mailMessage = new SimpleMailMessage();
				mailMessage.setTo(usuario.getEmail());
				mailMessage.setSubject("Completar Registro | Biblioteca2020");
				mailMessage.setFrom("edmech25@gmail.com");
				mailMessage.setText(
						"Buenas noches, hemos recibido tu peticiòn de registro a Biblioteca2020. Para confirmar tu cuenta, entrar aquì: "
								+ "http://localhost:8080/usuarios/cuenta-verificada?token="
								+ confirmationToken.getConfirmationToken());
				flash.addFlashAttribute("success", "El usuario ha sido registrado en la base de datos.");
				emailSenderService.sendEmail(mailMessage);

				model.addAttribute("titulo", "Registro exitoso");
				model.addAttribute("email", usuario.getEmail());
				return "/usuarios/registro-exitoso";
			} catch (Exception e) {
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findOnlyUsers());
				model.addAttribute("titulo", "Registro de Usuario");
				model.addAttribute("error", e.getMessage());
				return "/usuarios/perfil";
			}
		}		
	}

	@RequestMapping(value = "/cuenta-verificada", method = { RequestMethod.GET, RequestMethod.POST })
	public String verificarCuenta(Model model, @RequestParam("token") String confirmationToken) {
		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);

		if (token != null) {
			try {
				Usuario usuario = usuarioService.findByEmailIgnoreCase(token.getUsuario().getEmail());
				usuario.setEstado(true);
				usuarioService.update(usuario);
				model.addAttribute("titulo", "Cuenta Verificada");
				return "/usuarios/cuenta-verificada";
			} catch (Exception e) {
				model.addAttribute("error", "Error: " + e.getMessage());
				model.addAttribute("titulo", "Error al Registrar");
				return "/usuarios/error-registro";
			}		
		} else {
			model.addAttribute("error", "El enlace es invàlido!");
			return "/usuarios/error-registro";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/editarPerfil")
	public String editarPerfil(Map<String, Object> modelMap, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		modelMap.put("editable", true);
		modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Modificar Usuario");
		try {
			modelMap.put("usuario", usuario);
			return "/usuarios/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/editarPerfil")
	public String guardarPerfil(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			return "/usuarios/perfil";
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
		} else if (usuario.getFoto_usuario() == null || usuario.getFoto_usuario() == "") {
			usuario.setFoto_usuario("no-image.jpg");
		}

		try {
			usuarioService.update(usuario);
			flash.addFlashAttribute("warning",
					"La información de su perfil han sido actualizados en la base de datos.");
			status.setComplete();
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cambioPassword")
	public String cambioPasswordUser(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(usuario.getId());
		model.addAttribute("passwordForm", cambiarPassword);
		model.addAttribute("titulo", "Cambiar Password");
		return "/usuarios/cambio-password";
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping("/cambioPassword")
	public String cambioPasswordUser(@Valid CambiarPassword form, Model model, Errors errors, RedirectAttributes flash,
			Authentication authentication) {
		if (errors.hasErrors()) {
			String result = errors.getAllErrors().stream().map(x -> x.getDefaultMessage())
					.collect(Collectors.joining(", "));
			model.addAttribute("cambiarPasswordError", result);
			return "/usuarios/cambio-password";
		}

		try {
			usuarioService.cambiarPassword(form);
			flash.addFlashAttribute("success", "Password Actualizada");
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("passwordForm", form);
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/usuarios/cambio-password";
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/deshabilitarPerfil")
	public String deshabilitarPerfil(RedirectAttributes flash, Authentication authentication) {
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
			usuario.setEstado(false);
			usuarioService.update(usuario);
			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL USUARIO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			return "redirect:/login";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	// ----------------------------- ROLE ADMIN, SUPERVISOR, EMPLEADO

	// LISTADO DE USUARIOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping("/listar")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("usuarios", usuarioService.findAll());
		model.addAttribute("titulo", "Listado de Usuarios");
		return "usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_USER')")
	@GetMapping("/crear")
	public String crearFormUsuario(Map<String, Object> modelMap) {
		modelMap.put("usuario", new Usuario());
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_USER')")
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
		modelMap.put("passwordForm", new CambiarPassword(id));
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
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
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
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
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
