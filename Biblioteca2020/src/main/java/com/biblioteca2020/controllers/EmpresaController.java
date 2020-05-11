package com.biblioteca2020.controllers;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IEmpresaService;

@Controller
@RequestMapping("/empresas")
@SessionAttributes("empresa")
public class EmpresaController {

	@Autowired
	private IEmpresaService empresaService;

	@Autowired
	private IEmpleadoService empleadoService;

	// ############################# CRUD #############################
	// LISTADO DE EMPRESAS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/listar")
	public String listarEmpresas(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresa = empleado.getLocal().getEmpresa();
		model.addAttribute("titulo", "Datos de " + empleado.getLocal().getEmpresa().getRazonSocial());
		model.addAttribute("empresas", empresa);
		return "/empresas/listar";
	}

	// REGRESAR A LISTADO DE EMPRESAS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/empresas/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE EMPRESAS POR SU RUC MEDIANTE
	// AUTOCOMPLETADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@RequestMapping(value = "/cargar-empresas/{term}", produces = { "application/json" })
	public @ResponseBody Empresa cargarInputEmpresas(@PathVariable String term) {
		return empresaService.findByRucAndEstado(term, true);
	}

	// FORMULARIO DE EDICION DE EMPRESA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping(value = "/editar/{id}")
	public String editarEmpresa(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		model.addAttribute("editable", true);
		model.addAttribute("titulo", "Modificar datos de la empresa");
		try {
			Empresa empresa = empresaService.findOne(id);
			model.addAttribute("empresa", empresa);
			return "/empresas/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empresas/listar";
		}
	}

	// ACTUALIZAR LIBRO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@PostMapping(value = "/editar")
	public String guardarEmpresa(@Valid Empresa empresa, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("empresa", empresa);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos de la empresa");
			return "/empresas/crear";
		}
		try {
			empresaService.update(empresa);
			flash.addFlashAttribute("warning", "Los datos de la empresa " + empresa.getRazonSocial() + " (código "
					+ empresa.getId() + ") han sido actualizados en la base de datos");
			status.setComplete();
			return "redirect:/empresas/listar";
		} catch (Exception e) {
			model.addAttribute("empresa", empresa);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos de la empresa");
			model.addAttribute("error", e.getMessage());
			return "/empresas/crear";
		}
	}
}
