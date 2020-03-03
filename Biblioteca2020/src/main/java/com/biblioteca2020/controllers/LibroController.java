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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Empresa;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.IEmpresaService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.ILocalService;

@Controller
@RequestMapping("/libros")
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
	@GetMapping(value = "/listar/{id}")
	public String listarLibrosPorLocal(@PathVariable(value = "id") Long id, Model model, Principal principal) {
		// BUSCA EL EMPLEADO LOGUEADO
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		List<Libro> libros;
		try {
			// BUSCO LOS LIBROS POR SU LOCAL_ID (DESDE LA PANTALLA ANTERIOR)
			libros = libroService.fetchByIdWithLocalWithEmpresaWithEmpleado(id, empleado.getId());
			// BUSCA EL LOCAL POR SU LOCAL_ID (DESDE LA PANTALLA ANTERIOR)
			Local local = localService.findOne(id);
			model.addAttribute("libros", libros);
			// MANDO A LA VISTA EL ID PARA FUTUROS USOS
			model.addAttribute("id", id);
			model.addAttribute("titulo", "Listado de Libros de '" + local.getDireccion() + "'");
			return "/libros/listar";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "/home";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/cancelar/{id}")
	public String cancelar(@PathVariable(value = "id") Long id, ModelMap modelMap, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		try {
			List<Libro> libros = libroService.fetchByIdWithLocalWithEmpresaWithEmpleado(id, empleado.getId());
			modelMap.put("libros", libros);
			// RECUPERO EL ID_LOCAL DESDE EL LISTADO PARA REGRESAR
			modelMap.put("id", id);
			return "redirect:/libros/listar/" + id;
		} catch (Exception e) {
			modelMap.addAttribute("error", e.getMessage());
			return "/home";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/crear/{id}")
	public String crearLibro(@PathVariable(value = "id") Long id, Map<String, Object> model, 
			Principal principal) {
		// RECUPERO EL ID_LOCAL DESDE EL LISTADO PARA REGRESAR
		model.put("id", id);
		model.put("libro", new Libro());
		try {
			model.put("titulo", "Registro de Libro");
			List<Local> locales = localService.findOnlyById(id);
			model.put("locales", locales);
			return "/libros/crear";
		} catch (Exception e) {
			model.put("error", e.getMessage());
			return "/libros/crear";
		}
		
	}

	///TODO: TE QUEDASTE AQUÍ - 03.03.2020 - 3:12:00
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/crear")
	public String crearLibro(@Valid Libro libro, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empresaService.fetchByIdWithEmpleado(empleado.getId());
		model.addAttribute("empresaLocales", empresaLocales);
		if (result.hasErrors()) {
			List<Local> locales = localService.findAll();
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			return "/libros/crear";
		}
		try {
			libro.setEstado(true);
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre '" + libro.getTitulo() + "').");

			status.setComplete();
			return "redirect:/libros/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			List<Local> locales = localService.findAll();
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/editar/{id}")
	public String editarLibro(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			RedirectAttributes flash, Principal principal) {
		Libro libro = null;
		List<Local> locales = localService.findAll();
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empresaService.fetchByIdWithEmpleado(empleado.getId());
		modelMap.put("editable", true);
		modelMap.put("titulo", "Modificar Libro");
		modelMap.put("locales", locales);
		try {
			libro = libroService.findOne(id);
			modelMap.put("libro", libro);
			return "/libros/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/libros/listar/" + empresaLocales.getId();
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/editar")
	public String guardarLibro(@Valid Libro libro, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Principal principal) {
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empresaService.fetchByIdWithEmpleado(empleado.getId());
		model.addAttribute("empresaLocales", empresaLocales);
		if (result.hasErrors()) {
			List<Local> locales = localService.findAll();
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			return "/libros/editar";
		}
		try {
			libro.setEstado(true);
			libroService.update(libro);
			flash.addFlashAttribute("success",
					"El libro con código " + libro.getTitulo() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/libros/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			List<Local> locales = localService.findAll();
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			model.addAttribute("locales", locales);
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/deshabilitar/{id}")
	public String deshabilitarLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Principal principal) {
		Libro libro = null;
		Empleado empleado = empleadoService.findByUsername(principal.getName());
		Empresa empresaLocales = empresaService.fetchByIdWithEmpleado(empleado.getId());
		try {
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.save(libro);
			flash.addFlashAttribute("warning", "El libro '" + libro.getTitulo() + "' ha sido deshabilitado.");
			return "redirect:/libros/listar/" + empresaLocales.getId();
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/libros/listar/" + empresaLocales.getId();
		}
	}

	/*
	 * @RequestMapping(value = "/cargarCategorias/{term}", produces = {
	 * "application/json" }) public @ResponseBody List<Categoria>
	 * cargarCategorias(@PathVariable String term) { return
	 * categoriaService.findByNombreLikeIgnoreCase(term); }
	 * 
	 * @RequestMapping(value = "/cargarLocales/{term}", produces = {
	 * "application/json" }) public @ResponseBody List<Local>
	 * cargarLocales(@PathVariable String term) { return
	 * localService.findByNombreLikeIgnoreCase(term); }
	 */

	/*
	 * @PostMapping(value = "/crear") public String guardarLibro(@RequestParam(name
	 * = "id_local", required = false) Long id_local,
	 * 
	 * @RequestParam(name = "id_categoria", required = false) Long id_categoria,
	 * 
	 * @RequestParam(name = "fechaEdicion", required = false) String fechaEdicion,
	 * 
	 * @RequestParam(name = "descripcion", required = false) String descripcion,
	 * 
	 * @RequestParam(name = "autor", required = false) String autor,
	 * 
	 * @RequestParam(name = "nombre", required = false) String nombre,
	 * 
	 * @RequestParam(name = "totalPaginas", required = false) Integer totalPaginas,
	 * 
	 * @RequestParam(name = "version", required = false) Integer version,
	 * RedirectAttributes flash, SessionStatus status, Model model) {
	 * 
	 * // SE PODRÌA VALIDAR DE MANERA MAS PERSONALIZADA (CAMPO POR CAMPO) if
	 * (id_local == null || id_categoria == null || fechaEdicion == null ||
	 * fechaEdicion == "" || descripcion == null || autor == null || nombre == null
	 * || totalPaginas == null || version == null) { model.addAttribute("titulo",
	 * "Registro de Libros"); model.addAttribute("error",
	 * "Rellenar todos los campos."); return "/libros/crear"; }
	 * 
	 * Categoria categoriaLibro = categoriaService.findOne(id_categoria);
	 * 
	 * System.out.println(categoriaLibro.getNombre());
	 * 
	 * Libro libroNuevo = new Libro();
	 * 
	 * libroNuevo.setCategoria(categoriaLibro);
	 * 
	 * Local localLibro = localService.findOne(id_local);
	 * 
	 * System.out.println(localLibro);
	 * 
	 * libroNuevo.setLocal(localLibro);
	 * 
	 * Date fechaEdicionLibro; try { fechaEdicionLibro = new
	 * SimpleDateFormat("yyyy-mm-dd").parse(fechaEdicion);
	 * libroNuevo.setFechaEdicion(fechaEdicionLibro);
	 * System.out.println(libroNuevo.getFechaEdicion()); } catch (ParseException e)
	 * { e.printStackTrace(); }
	 * 
	 * libroNuevo.setAutor(autor); System.out.println(libroNuevo.getAutor());
	 * 
	 * libroNuevo.setDescripcion(descripcion);
	 * System.out.println(libroNuevo.getDescripcion());
	 * 
	 * libroNuevo.setNombre(nombre); System.out.println(libroNuevo.getNombre());
	 * 
	 * libroNuevo.setTotalPaginas(totalPaginas);
	 * System.out.println(libroNuevo.getTotalPaginas());
	 * 
	 * libroNuevo.setVersion(version); System.out.println(libroNuevo.getVersion());
	 * 
	 * libroNuevo.setEstado(true); System.out.println(libroNuevo.getEstado());
	 * 
	 * libroService.save(libroNuevo); flash.addFlashAttribute("success",
	 * "El libro ha sido creado correctamente."); status.setComplete(); return
	 * "redirect:/libros/listar"; }
	 */
}
