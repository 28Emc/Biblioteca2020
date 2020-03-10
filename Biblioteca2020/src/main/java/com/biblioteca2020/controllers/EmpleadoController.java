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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/listar")
	public String listarEmpleados(Model model, Principal principal) {
		model.addAttribute("empleado", new Empleado());
		// AQUI TENGO QUE MOSTAR EL LISTADO DE EMPLEADOS DE ESA EMPRESA
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		// SI SOY ADMIN, VEO MI REGISTRO, SI NO, LO OCULTO
		// MÉTODO TEMPORAL, YA QUE EL ADMIN NO VA A PERTENECER A LOS 'EMPLEADOS'
		if (empleado.getRoles().toString().contains("ROLE_ADMIN")) {
			model.addAttribute("empleados",
					empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getEmpresa().getId()));
		} else {
			model.addAttribute("empleados",
					empleadoService.fetchByIdWithLocalWithEmpresaNotAdmin(empleado.getLocal().getEmpresa().getId()));
		}
		model.addAttribute("titulo", "Listado de Empleados");
		return "empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap) {
		return "redirect:/empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/crear")
	public String crearFormEmpleado(Map<String, Object> modelMap, Principal principal, RedirectAttributes flash) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		try {
			System.out.println(localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			modelMap.put("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			modelMap.put("empleado", new Empleado());
			modelMap.put("roles", roleService.findEmpleadoAndSupervisor());
			modelMap.put("titulo", "Registro de Empleado");
			return "empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/crear")
	public String crearEmpleado(@Valid Empleado empleado, BindingResult result, Model model,
			Map<String, Object> modelMap, SessionStatus status, RedirectAttributes flash, Principal principal) {
		if (result.hasErrors()) {
			model.addAttribute("empleado", empleado);
			try {
				modelMap.put("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("roles", roleService.findEmpleadoAndSupervisor());
			model.addAttribute("titulo", "Registro de Empleado");
			return "/empleados/crear";
		}
		try {
			empleadoService.save(empleado);
			flash.addFlashAttribute("success",
					"El empleado ha sido registrado en la base de datos (Código " + empleado.getId() + ")");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				modelMap.put("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
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
	public String editarFormEmpleado(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			Principal principal, RedirectAttributes flash) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
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
			modelMap.put("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "/empleados/crear";
		}

		Empleado empleadoEditable = null;
		modelMap.put("editable", true);
		modelMap.put("roles", roleService.findEmpleadoAndSupervisor());
		modelMap.put("titulo", "Modificar Empleado");

		try {
			empleadoEditable = empleadoService.findById(id);
			modelMap.put("empleado", empleadoEditable);
			return "/empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/editar")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, Principal principal) {
		Empleado empleadoLogueado = empleadoService.findByUsername(principal.getName());
		if (result.hasErrors()) {
			try {
				modelMap.put("localesList", localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
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
		try {
			empleadoService.update(empleado);
			flash.addFlashAttribute("warning",
					"El empleado con código " + empleado.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				modelMap.put("localesList", localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
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
			flash.addFlashAttribute("info",
					"El empleado con código " + empleado.getId() + " ha sido deshabilitado.");
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	/*
	 * @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	 * 
	 * @RequestMapping(value = "/eliminar/{id}") public String
	 * eliminarEmpleado(@PathVariable(value = "id") Long id, RedirectAttributes
	 * flash) { Empleado empleado= null; try { empleado =
	 * empleadoService.findById(id); flash.addFlashAttribute("error",
	 * "El empleado con código " + empleado.getId() +
	 * " ha sido eliminado de la base de datos.");
	 * empleadoService.borrarEmpleado(id); return "redirect:/empleados/listar"; }
	 * catch (Exception e) { flash.addFlashAttribute("error", e.getMessage());
	 * System.out.println(e.getMessage()); return "redirect:/empleados/listar"; } }
	 */
}
