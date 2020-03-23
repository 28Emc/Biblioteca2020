package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
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

	// LISTADO DE EMPLEADOS:
	// - SI SOY ADMIN, VEO MI REGISTRO
	// - SI NO, LO OCULTO
	// MÉTODO TEMPORAL, YA QUE EL ADMIN NO VA A PERTENECER A LOS 'EMPLEADOS'
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
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
		model.addAttribute("titulo", "Listado de Empleados");
		return "empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/crear")
	public String crearFormEmpleado(Model model, Authentication authentication,
			RedirectAttributes flash) {
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/editar/{id}")
	public String editarFormEmpleado(@PathVariable(value = "id") Long id, Model model,
			RedirectAttributes flash, Authentication authentication) {

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		// AQUI VALIDO SI EL USUARIO LOGUEADO ES ADMIN Y PUEDE EDITAR SU MISMO REGISTRO
		// EL ADMIN DEBE SER CAMBIADO DE TABLA 'EMPLEADO' A 'USUARIO'
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/editar")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication, @RequestParam("foto_emp") MultipartFile foto) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoLogueado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			try {
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
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
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
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
	
	@PreAuthorize("hasAnyRole('ROLE_SUPERVISOR')")
	@GetMapping("/perfilSupervisor")
	public String perfilSupervisor() {
		return "/empleados/perfilSupervisor";
	}
	
	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO')")
	@GetMapping("/perfilEmpleado")
	public String perfilEmpleado() {
		return "/empleados/perfilEmpleado";
	}
}
