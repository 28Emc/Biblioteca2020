package com.biblioteca2020.controllers;

import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IEmpresaService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.ILocalService;

@Controller
@RequestMapping("/locales/listar")
@SessionAttributes("libro")
public class LibroController {

	@Autowired
	private ILibroService libroService;

	@Autowired
	private ILocalService localService;

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IEmpresaService empresaService;

	// LISTADO DE LIBROS POR LOCAL Y USUARIO LOGUEADO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/{idLocal}/libros/listar")
	public String listarLibrosPorLocal(@PathVariable(value = "idLocal") Long idLocal, Model model,
			Principal principal) {
		// BUSCA EL EMPLEADO LOGUEADO
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		List<Libro> libros;
		
		try {
			// libros = libroService.fetchByIdWithLocalWithEmpresaWithEmpleado(idLocal,
			// empleado.getId());
			// BUSCA EL LOCAL POR SU LOCAL_ID (DESDE LA PANTALLA ANTERIOR) PARA PODER ARMAR
			// DE VUELTA LA RUTA DE ESE LOCAL
			Local local = localService.findOne(idLocal);
			// MANDO A LA VISTA EL ID PARA FUTUROS USOS
			model.addAttribute("idLocal", idLocal);
			model.addAttribute("titulo", "Listado de Libros de '" + local.getDireccion() + "'");
			//return "/libros/listar";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "/home";
		}
		
		try {
			// BUSCO LOS LIBROS POR SU LOCAL_ID Y POR EL ID_EMPLEADO (DESDE LA PANTALLA ANTERIOR)
			libros = libroService.fetchByIdWithLocalesWithEmpleado(idLocal, empleado.getId());
			model.addAttribute("libros", libros);
			//return "/libros/listar";
		} catch (Exception e1) {
			model.addAttribute("error", e1.getMessage());
			return "/home";
		}		
		
		return "/libros/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/{idLocal}/libros/cancelar")
	public String cancelar(@PathVariable(value = "idLocal") Long idLocal, ModelMap modelMap, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		try {
			// List<Libro> libros =
			// libroService.fetchByIdWithLocalWithEmpresaWithEmpleado(idLocal,
			// empleado.getId());
			List<Libro> libros = libroService.fetchByIdWithLocalesWithEmpleado(idLocal, empleado.getId());
			modelMap.put("libros", libros);
			// RECUPERO EL ID_LOCAL DESDE EL LISTADO PARA REGRESAR
			modelMap.put("idLocal", idLocal);
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		} catch (Exception e) {
			modelMap.addAttribute("error", e.getMessage());
			return "/libros/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/{idLocal}/libros/crear")
	public String crearLibro(@PathVariable(value = "idLocal") Long idLocal, Map<String, Object> model,
			Principal principal) {
		// RECUPERO EL ID_LOCAL DESDE EL LISTADO PARA REGRESAR
		model.put("idLocal", idLocal);
		model.put("libro", new Libro());
		try {
			model.put("titulo", "Registro de Libro");
			List<Local> locales = localService.findOnlyById(idLocal);
			model.put("locales", locales);
			return "/libros/crear";
		} catch (Exception e) {
			model.put("error", e.getMessage());
			return "/libros/crear";
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/{idLocal}/libros/crear")
	public String crearLibro(@PathVariable(value = "idLocal") Long idLocal, @Valid Libro libro, BindingResult result,
			Model model, SessionStatus status, RedirectAttributes flash, Principal principal) {
		model.addAttribute("idLocal", idLocal);
		// Empleado empleado = empleadoService.findByUsername(principal.getName());
		// Empresa empresaLocales =
		// empresaService.fetchByIdWithLocalWithEmpleado(empleado.getId());
		// model.addAttribute("empresaLocales", empresaLocales);
		if (result.hasErrors()) {
			List<Local> locales = localService.findOnlyById(idLocal);
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			model.addAttribute("idLocal", idLocal);
			return "/libros/crear";
		}
		try {
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre '" + libro.getTitulo() + "').");
			status.setComplete();
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		} catch (Exception e) {
			List<Local> locales = localService.findAll();
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			model.addAttribute("idLocal", idLocal);
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/{idLocal}/libros/editar/{id}")
	public String editarLibro(@PathVariable(value = "id") Long idlibro, @PathVariable(value = "idLocal") Long idLocal,
			Map<String, Object> modelMap, RedirectAttributes flash, Principal principal) {
		Libro libro = null;
		List<Local> locales = localService.findOnlyById(idLocal);
		modelMap.put("editable", true);
		modelMap.put("titulo", "Modificar Libro");
		modelMap.put("locales", locales);
		modelMap.put("idLocal", idLocal);
		try {
			libro = libroService.findOne(idlibro);
			modelMap.put("libro", libro);
			return "/libros/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			modelMap.put("idLocal", idLocal);
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/{idLocal}/libros/editar")
	public String guardarLibro(@PathVariable(value = "idLocal") Long idLocal, @Valid Libro libro, BindingResult result,
			Model model, SessionStatus status, RedirectAttributes flash, Principal principal) {
		model.addAttribute("empresaLocales", idLocal);
		model.addAttribute("idLocal", idLocal);
		if (result.hasErrors()) {
			List<Local> locales = localService.findOnlyById(idLocal);
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			model.addAttribute("idLocal", idLocal);
			return "/libros/editar";
		}
		try {
			libroService.update(libro);
			flash.addFlashAttribute("success",
					"El libro con código " + libro.getTitulo() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		} catch (Exception e) {
			List<Local> locales = localService.findOnlyById(idLocal);
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			model.addAttribute("idLocal", idLocal);
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{idLocal}/libros/deshabilitar/{id}")
	public String deshabilitarLibro(@PathVariable(value = "idLocal") Long idLocal, @PathVariable(value = "id") Long id,
			RedirectAttributes flash, Principal principal) {
		Libro libro = null;
		try {
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.update(libro);
			flash.addFlashAttribute("warning", "El libro '" + libro.getTitulo() + "' ha sido deshabilitado.");
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/listar/" + idLocal + "/libros/listar";
		}
	}
}
