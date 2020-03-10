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
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILocalService;

@Controller
@RequestMapping("/locales")
@SessionAttributes("local")
public class LocalController {

	@Autowired
	private ILocalService localService;

	@Autowired
	private IEmpleadoService empleadoService;

	/*
	 * @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	 * 
	 * @GetMapping(value = "/listar") public String listarLocales(Model model) {
	 * model.addAttribute("titulo", "Listado de Locales");
	 * model.addAttribute("locales", localService.findAll()); return
	 * "/locales/listar"; }
	 */

	// LOS SUPERVISORES PUEDEN VER EL LOCAL ANEXO A SU
	// EMPRESA, EN CASO CONTRARIO NO TENGO ACCESO
	@PreAuthorize("hasAnyRole('ROLE_SUPERVISOR', 'ROLE_ADMIN')")
	@GetMapping(value = "/listar/{id}")
	public String listarLocalesPorEmpresa(@PathVariable(value = "id") Long id, Model model, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Local local;
		// VALIDO QUE EL LOCAL EXISTE
		try {
			localService.findById(id);
		} catch (Exception e1) {
			model.addAttribute("error", e1.getMessage());
			return "/home";
		}
		// VALIDO QUE TENGA ACCESO AL LOCAL
		try {
			local = localService.fetchByIdWithEmpresaWithEmpleado(id, empleado.getId());
			model.addAttribute("titulo",
					"Listado de Locales de '" + empleado.getLocal().getEmpresa().getRazonSocial() + "'");
			model.addAttribute("locales", local);
			return "/locales/listar";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "/home";
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		modelMap.put("empresaLocales", empresaLocales);
		return "redirect:/locales/listar/" + empresaLocales.getId();
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/crear")
	public String crearLocal(Map<String, Object> model, Principal principal) {
		model.put("titulo", "Registro de Local");
		model.put("local", new Local());
		// AQUÍ TENGO QUE LLAMAR A LOS DATOS DE LA EMPRESA DE ESE LOCAL
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		model.put("empresaLocales", empresaLocales);
		return "/locales/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearLocal(@Valid Local local, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		model.addAttribute("empresaLocales", empresaLocales);
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Local");
			model.addAttribute("local", local);
			model.addAttribute("empresaLocales", empresaLocales);
			return "/locales/crear";
		}
		try {
			localService.save(local);
			flash.addFlashAttribute("success",
					"El local ha sido registrado en la base datos (Código " + local.getId() + ").");
			status.setComplete();
			return "redirect:/locales/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			model.addAttribute("titulo", "Formulario de Local");
			model.addAttribute("local", local);
			model.addAttribute("error", e.getMessage());
			model.addAttribute("empresaLocales", empresaLocales);
			return "/locales/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/editar/{id}")
	public String editarLocal(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			RedirectAttributes flash, Principal principal) {
		Local local = null;
		modelMap.put("editable", true);
		modelMap.put("titulo", "Modificar Local");
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		modelMap.put("empresaLocales", empresaLocales);
		try {
			local = localService.findById(id);
			modelMap.put("local", local);
			return "/locales/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/listar/" + empresaLocales.getId();
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String guardarLocal(@Valid Local local, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		modelMap.put("empresaLocales", empresaLocales);
		if (result.hasErrors()) {
			model.addAttribute("local", local);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Local");
			return "/locales/editar";
		}
		try {
			localService.update(local);
			flash.addFlashAttribute("warning", "El local con dirección '" + local.getDireccion() + "'(código "
					+ local.getId() + ") ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/locales/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			model.addAttribute("local", local);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Local");
			model.addAttribute("error", e.getMessage());
			return "/locales/crear";
		}
	}

	/*@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarLocal(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Principal principal) {
		Local local = null;
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empleado.getLocal().getEmpresa();
		try {
			local = localService.findById(id);
			local.setEstado(false);
			localService.update(local);
			flash.addFlashAttribute("warning", "El local con dirección '" + local.getDireccion() + "'(código "
					+ local.getId() + ") ha sido deshabilitado.");
			return "redirect:/locales/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/listar/" + empresaLocales.getId();
		}
	}*/
}
