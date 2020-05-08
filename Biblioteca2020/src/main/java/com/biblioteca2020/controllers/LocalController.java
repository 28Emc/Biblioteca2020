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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IEmpresaService;
import com.biblioteca2020.models.service.ILocalService;

@Controller
@RequestMapping("/locales")
@SessionAttributes("local")
public class LocalController {

	@Autowired
	private ILocalService localService;

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IEmpresaService empresaService;

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping(value = "/listar")
	public String listarLocales(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Listado de locales de " + empleado.getLocal().getEmpresa().getRazonSocial());
		model.addAttribute("locales", localService.findAll());
		return "/locales/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/listar/{id}")
	public String listarLocalesPorEmpresa(@PathVariable(value = "id") Long id, Model model,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Local local;
		String ruta = "";
		// VALIDO QUE EL LOCAL EXISTE Y QUE PUEDA ACCEDER A ÉL
		try {
			localService.findById(id);
			local = localService.fetchByIdWithEmpresaWithEmpleado(id, empleado.getId());
			model.addAttribute("titulo", "Listado de locales de " + empleado.getLocal().getEmpresa().getRazonSocial());
			model.addAttribute("locales", local);
			ruta = "/locales/listar";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			ruta = "/home";
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping("/cancelar")
	public String cancelar(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresaLocales = empresaService.fetchByIdWithLocal(empleado.getLocal().getEmpresa().getId());
		String ruta = "";
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				ruta = "redirect:/locales/listar";
				break;
			case "[ROLE_ADMIN]":
				model.addAttribute("empresaLocales", empresaLocales);
				ruta = "redirect:/locales/listar/" + empresaLocales.getId();
				break;
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/crear")
	public String crearLocal(Model model, Authentication authentication) {
		model.addAttribute("titulo", "Registro de local nuevo");
		model.addAttribute("local", new Local());
		// AQUÍ TENGO QUE LLAMAR A LOS DATOS DE LA EMPRESA PARA EL NUEVO LOCAL
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresaLocales = empresaService.fetchByIdWithLocal(empleado.getLocal().getEmpresa().getId());
		String ruta = "";
		try {
			model.addAttribute("empresaLocales", empresaLocales);
			ruta = "/locales/crear";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			ruta = "/locales/listar";
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String guardarLocal(@Valid Local local, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication) throws Exception {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresaLocales = empresaService.fetchByIdWithLocal(empleado.getLocal().getEmpresa().getId());
		model.addAttribute("empresaLocales", empresaLocales);
		String ruta = "";
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Registro de local nuevo");
			model.addAttribute("local", local);
			model.addAttribute("empresaLocales", empresaLocales);
			model.addAttribute("error", "El campo dirección es obligatorio");
			return "/locales/crear";
		}
		try {
			localService.save(local);
			flash.addFlashAttribute("success",
					"El local ha sido registrado en la base datos (código " + local.getId() + ")");
			status.setComplete();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					ruta = "redirect:/locales/listar";
					break;
				case "[ROLE_ADMIN]":
					ruta = "redirect:/locales/listar/" + empresaLocales.getId();
					break;
			}
		} catch (Exception e) {
			model.addAttribute("titulo", "Registro de local nuevo");
			model.addAttribute("local", local);
			model.addAttribute("error", e.getMessage());
			model.addAttribute("empresaLocales", empresaLocales);
			ruta = "/locales/crear";
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/editar/{id}")
	public String editarLocal(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash,
			Authentication authentication) {
		Local local = null;
		model.addAttribute("editable", true);
		model.addAttribute("titulo", "Modificar datos del local");
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresaLocales = empresaService.fetchByIdWithLocal(empleado.getLocal().getEmpresa().getId());
		model.addAttribute("empresaLocales", empresaLocales);
		String ruta = "";
		try {
			local = localService.findById(id);
			model.addAttribute("local", local);
			ruta = "/locales/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					ruta = "redirect:/locales/listar";
					break;
				case "[ROLE_ADMIN]":
					ruta = "redirect:/locales/listar/" + empresaLocales.getId();
					break;
			}
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	// HAY UN BUG CON LA ANOTACIÓN @VALID EN ESTE CONTROLADOR SOLAMENTE
	public String actualizarLocal(Local local, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empresa empresaLocales = empresaService.fetchByIdWithLocal(empleado.getLocal().getEmpresa().getId());
		model.addAttribute("empresaLocales", empresaLocales);
		String ruta = "";
		if (result.hasErrors()) {
			model.addAttribute("local", local);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos del local");
			return "/locales/crear";
		}
		try {
			localService.update(local);
			flash.addFlashAttribute("warning", "Los datos del local con dirección " + local.getDireccion() + "(código "
					+ local.getId() + ") han sido actualizados en la base de datos");
			status.setComplete();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					ruta = "redirect:/locales/listar";
					break;
				case "[ROLE_ADMIN]":
					ruta = "redirect:/locales/listar/" + empresaLocales.getId();
					break;
			}
		} catch (Exception e) {
			model.addAttribute("local", local);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos del local");
			model.addAttribute("error", e.getMessage());
			ruta = "/locales/crear";
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarLocal(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Local local = null;
		String ruta = "";
		try {
			local = localService.findById(id);
			local.setEstado(false);
			localService.update(local);
			flash.addFlashAttribute("warning", "El local con dirección '" + local.getDireccion() + "'(código "
					+ local.getId() + ") ha sido deshabilitado");
			ruta = "redirect:/locales/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			ruta = "redirect:/locales/listar";
		}
		return ruta;
	}
}
