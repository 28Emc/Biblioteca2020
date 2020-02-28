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
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.service.IEmpresaService;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
@RequestMapping("/empresas")
@SessionAttributes("empresa")
public class EmpresaController {

	@Autowired
	private IEmpresaService empresaService;

	@GetMapping(value = "/listar")
	public String listarEmpresas(Model model) {
		model.addAttribute("titulo", "Listado de Empresas");
		model.addAttribute("empresas", empresaService.findAll());
		return "empresas/listar";
	}

	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap) {
		return "redirect:/empresas/listar";
	}

	@GetMapping(value = "/crear")
	public String crearEmpresa(Map<String, Object> model) {
		model.put("titulo", "Registro de Empresa");
		model.put("empresa", new Empresa());
		return "empresas/crear";
	}

	@PostMapping(value = "/crear")
	public String guardarEmpresa(@Valid Empresa empresa, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("empresa", empresa);
			model.addAttribute("titulo", "Registro de Empresa");
			return "/empresas/crear";
		}
		try {
			empresa.setEstado(true);
			empresaService.save(empresa);
			flash.addFlashAttribute("success",
					"La empresa ha sido registrada en la base de datos (Código " + empresa.getId() + ")");
			status.setComplete();
			return "redirect:/empresas/listar";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("empresa", empresa);
			model.addAttribute("titulo", "Registro de Empresa");
			return "/empresas/crear";
		}

	}

	@GetMapping(value = "/crear/{id}")
	public String editarFormEmpresa(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			RedirectAttributes flash) {
		Empresa empresa = null;
		modelMap.put("editable", true);
		modelMap.put("titulo", "Modificar Empresa");
		try {
			empresa = empresaService.findOne(id);
			modelMap.put("empresa", empresa);
			flash.addFlashAttribute("info", "Los datos de la empresa con código " + empresa.getId()
					+ " han sido actualizados en la base de datos.");
			return "/empresas/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empresas/listar";
		}
	}
	
	@PostMapping(value = "/editar")
	public String guardarEmpresa(@Valid Empresa empresa, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, Principal principal) {
		if (result.hasErrors()) {
			model.addAttribute("empresa", empresa);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Empresa");
			return "/empresas/editar";
		}
		try {
			empresaService.update(empresa);
			flash.addFlashAttribute("info",
					"La empresa con código " + empresa.getId() + " ha sido actualizada en la base de datos.");
			status.setComplete();
			return "redirect:/empresas/listar";
		} catch (Exception e) {
			model.addAttribute("empresa", empresa);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Empresa");
			model.addAttribute("error", e.getMessage());
			return "/empresas/crear";
		}
	}

	@RequestMapping(value = "/eliminar/{id}")
	public String eliminarEmpresa(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Empresa empresa = null;
		try {
			empresa = empresaService.findOne(id);
			flash.addFlashAttribute("error",
					"La empresa con código " + empresa.getId() + " ha sido eliminada de la base de datos.");
			empresaService.delete(id);
			return "redirect:/empresas/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empresas/listar";
		}

	}

	@RequestMapping(value = "/deshabilitar/{id}")
	public String deshabilitarEmpresa(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Empresa empresa = null;
		try {
			empresa = empresaService.findOne(id);
			empresa.setEstado(false);
			empresaService.update(empresa);
			flash.addFlashAttribute("warning", "La empresa con código " + empresa.getId() + " ha sido deshabilitada.");
			return "redirect:/empresas/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empresas/listar";
		}
	}
}
