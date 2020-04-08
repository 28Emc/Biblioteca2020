package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILocalService;
import com.biblioteca2020.models.service.IRoleService;

@Controller
@RequestMapping("/empleados")
@SessionAttributes("empleado")
public class EmpleadoController {

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private ILocalService localService;

	@Autowired
	private IRoleService roleService;

	// ############################## ROLE EMPLEADO, ROLE ADMIN
	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/editarPerfil")
	public String editarPerfil(Map<String, Object> modelMap, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		modelMap.put("editable", true);
		modelMap.put("passwordForm", new CambiarPassword(empleado.getId()));
		modelMap.put("roles", roleService.findEmpleadoAndSupervisor());
		modelMap.put("titulo", "Modificar Perfil");
		try {
			modelMap.put("empleado", empleado);
			return "/empleados/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@PostMapping(value = "/editarPerfil")
	public String guardarPerfil(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_emp") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(empleado.getId()));
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Modificar Perfil");
			return "/empleados/perfil";
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				empleado.setFoto_empleado(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		} else if (empleado.getFoto_empleado() == null || empleado.getFoto_empleado() == "") {
			empleado.setFoto_empleado("no-image.jpg");
		}

		try {
			empleadoService.update(empleado);
			flash.addFlashAttribute("warning",
					"La información de su perfil han sido actualizados en la base de datos.");
			status.setComplete();
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(empleado.getId()));
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("error", e.getMessage());
			return "/empleados/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/cancelarPerfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/cambioPassword")
	public String cambioPasswordEmpleado(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(empleado.getId());
		model.addAttribute("cambiarPassword", cambiarPassword);
		model.addAttribute("titulo", "Cambiar Password");
		return "/empleados/cambio-password";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@PostMapping("/cambioPassword")
	public String cambioPasswordEmpleado(@Valid CambiarPassword cambiarPassword, BindingResult resultForm, Model model,
			RedirectAttributes flash, Authentication authentication) {
		
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getPasswordActual().equals("") || cambiarPassword.getNuevaPassword().equals("")
					|| cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios");
				model.addAttribute("titulo", "Cambiar Password");
				return "/empleados/cambio-password";
			}
			String result = resultForm.getAllErrors().stream().map(x -> x.getDefaultMessage())
					.collect(Collectors.joining(", "));
			model.addAttribute("cambiarPasswordError", result);
			return "/empleados/cambio-password";
		}
		try {
			empleadoService.cambiarPassword(cambiarPassword);
			flash.addFlashAttribute("success", "Password Actualizada");
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/empleados/cambio-password";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitarPerfil")
	public String deshabilitarPerfil(RedirectAttributes flash, Authentication authentication) {
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
			empleado.setEstado(false);
			empleadoService.update(empleado);
			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL EMPLEADO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			return "redirect:/login";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/perfil";
		}
	}

	// ############################## ROLE ADMIN
	// LISTADO DE EMPLEADOS
	// SI SOY ADMIN, VEO MI REGISTRO
	// SI NO, LO OCULTO
	// MÉTODO TEMPORAL, YA QUE EL ADMIN NO VA A PERTENECER A LOS 'EMPLEADOS'
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/listar")
	public String listarEmpleados(Model model, Authentication authentication, Principal principal) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());

		switch (userDetails.getAuthorities().toString()) {
		case "[ROLE_ADMIN]":
			model.addAttribute("empleados",
					empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getEmpresa().getId()));
			break;
		case "[ROLE_SUPERVISOR]":
			model.addAttribute("empleados",
					empleadoService.fetchByIdWithLocalWithEmpresaNotAdmin(empleado.getLocal().getEmpresa().getId()));
			break;
		}

		model.addAttribute("empleado", new Empleado());
		model.addAttribute("titulo",
				"Listado de Empleados de '" + empleado.getLocal().getEmpresa().getRazonSocial() + "'");
		return "empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/crear")
	public String crearFormEmpleado(Model model, Authentication authentication, RedirectAttributes flash) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		try {
			model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			model.addAttribute("empleado", new Empleado());
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Registro de Empleado");
			return "empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_emp") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("empleado", empleado);
			try {
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Registro de Empleado");
			return "/empleados/crear";
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				empleado.setFoto_empleado(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}
		try {
			empleadoService.save(empleado);
			flash.addFlashAttribute("success",
					"El empleado ha sido registrado en la base de datos (Código " + empleado.getId() + ")");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			} catch (Exception e2) {
				flash.addFlashAttribute("error", e2.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Registro de Empleado");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/editar/{id}")
	public String editarFormEmpleado(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		// AQUI VALIDO SI EL USUARIO LOGUEADO ES ADMIN Y PUEDE EDITAR SU MISMO REGISTRO
		try {
			Empleado empleadoAdmin = empleadoService.findById(id);
			if (empleadoAdmin.getRoles().toString().contains("ROLE_ADMIN") == true
					&& empleado.getRoles().toString().contains("ROLE_ADMIN") == false) {
				flash.addFlashAttribute("error", "No tienes permiso para acceder a este recurso.");
				return "redirect:/empleados/listar";
			}
		} catch (Exception e1) {
			flash.addFlashAttribute("error", e1.getMessage());
			return "redirect:/empleados/listar";
		}
		// FIN VALIDACION ADMIN
		try {
			model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
		Empleado empleadoEditable = null;
		model.addAttribute("editable", true);
		model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
		model.addAttribute("titulo", "Modificar Empleado");
		try {
			empleadoEditable = empleadoService.findById(id);
			model.addAttribute("empleado", empleadoEditable);
			return "/empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication, @RequestParam("foto_emp") MultipartFile foto) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoLogueado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			try {
				model.addAttribute("localesList",
						localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Modificar Empleado");
			return "/empleados/editar";
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				empleado.setFoto_empleado(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}
		try {
			empleadoService.update(empleado);
			flash.addFlashAttribute("warning",
					"El empleado con código " + empleado.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				model.addAttribute("localesList",
						localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
			} catch (Exception e2) {
				flash.addFlashAttribute("error", e2.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Modificar Empleado");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarEmpleado(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Empleado empleado = null;
		try {
			empleado = empleadoService.findById(id);
			empleado.setEstado(false);
			empleadoService.update(empleado);
			flash.addFlashAttribute("info", "El empleado con código " + empleado.getId() + " ha sido deshabilitado.");
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/perfilAdmin")
	public String perfilAdmin() {
		return "/empleados/perfilAdmin";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO')")
	@GetMapping("/perfilEmpleado")
	public String perfilEmpleado() {
		return "/empleados/perfilEmpleado";
	}
}
