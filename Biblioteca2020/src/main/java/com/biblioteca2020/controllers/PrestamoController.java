package com.biblioteca2020.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.IPrestamoService;
import com.biblioteca2020.models.service.IUsuarioService;

@Controller
@RequestMapping("/prestamos")
@SessionAttributes("prestamos")
public class PrestamoController {

	@Autowired
	private IPrestamoService prestamoService;

	@Autowired
	private ILibroService libroService;

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IUsuarioService usuarioService;

	// ############################ ADMIN, EMPLEADO ############################
	// LISTADO POR ROLES
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/listar")
	public String listarPrestamosPorRol(Model model, Authentication authentication) {
		// OBTENER USUARIO LOGUEADO ACTUALMENTE
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		// MOSTRAR LISTADO DE ACUERDO A ROL
		switch (userDetails.getAuthorities().toString()) {
		// ADMIN: VE PRESTAMOS DEL LOCAL ENTERO
		case "[ROLE_ADMIN]":
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado(empleado.getLocal().getId()));
			break;
		// EMPLEADO: VE PRESTAMOS REALIZADOS A SU NOMBRE
		case "[ROLE_EMPLEADO]":
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(empleado.getId()));
			break;
		}
		model.addAttribute("titulo", "Listado de Préstamos");
		model.addAttribute("confirma", true);
		return "/prestamos/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping(value = "/cancelar")
	public String cancelar() {
		return "redirect:/prestamos/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE LIBROS ACTIVOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargar-libros/{term}", produces = { "application/json" })
	public @ResponseBody List<Libro> cargarLibros(@PathVariable String term, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		return libroService.findByTituloLikeIgnoreCaseAndLocalAndEstado(term, empleadoPrestamo.getLocal().getId(),
				true);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE USUARIOS ACTIVOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargar-usuarios/{term}", produces = { "application/json" })
	public @ResponseBody List<Usuario> cargarUsuarios(@PathVariable String term) {
		return usuarioService.findByNroDocumentoAndEstado(term, true);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE EMPLEADOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargar-empleados/{term}", produces = { "application/json" })
	public @ResponseBody List<Empleado> cargarEmpleados(@PathVariable String term) {
		return empleadoService.findAllByNroDocumentoAndEstado(term, true);
	}

	// CARGA DE FORMULARIO DE CREACIÓN DE PRÉSTAMOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/crear")
	public String crearPrestamo(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		model.addAttribute("titulo", "Creación de Préstamo");
		model.addAttribute("prestamo", new Prestamo());
		model.addAttribute("empleado", empleadoPrestamo);
		return "/prestamos/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@PostMapping(value = "/crear")
	public String guardarPrestamo(@Valid Prestamo prestamo, BindingResult result, RedirectAttributes flash,
			SessionStatus status, Model model, Authentication authentication) {
		// EMPLEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		prestamo.setEmpleado(empleadoPrestamo);
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Creación de Préstamo");
			model.addAttribute("prestamo", prestamo);
			return "/prestamos/crear";
		}
		// LIBRO
		try {
			Libro libroPrestamo = libroService.findOne(prestamo.getLibro().getId());
			prestamo.setLibro(libroPrestamo);
			// ACTUALIZACIÓN DE STOCK
			if (libroPrestamo.getStock() <= 0) {
				model.addAttribute("error", "Lo sentimos, no hay stock suficiente del libro seleccionado ("
						+ libroPrestamo.getTitulo() + ")");
				return "redirect:/prestamos/listar";
			} else {
				libroPrestamo.setStock(libroPrestamo.getStock() - 1);
			}
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
		}
		// USUARIO
		try {
			Usuario usuarioPrestamo = usuarioService.findById(prestamo.getUsuario().getId());
			prestamo.setUsuario(usuarioPrestamo);
		} catch (Exception e1) {
			model.addAttribute("error", e1.getMessage());
			return "/prestamos/crear";
		}
		// FECHA DESPACHO
		Date fechaDespacho = new Date();
		prestamo.setFecha_despacho(fechaDespacho);
		// USO CALENDAR PARA MOSTRAR LA FECHA DE DEVOLUCION
		Calendar calendar = Calendar.getInstance(new Locale("es", "ES"));
		calendar.setTime(prestamo.getFecha_devolucion());
		// MOSTRAR FECHA POR DIA, MES Y ANIO
		String anio = String.valueOf(calendar.get(Calendar.YEAR));
		String mes = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es", "ES"));
		String dia = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		String fechaPrestamoHoy = dia + " de " + mes + " " + anio;
		// OBSERVACIONES
		prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo()
				+ " ha sido programado para su devolución el día " + prestamo.getFecha_devolucion()
				+ ", por el empleado: " + empleadoPrestamo.getNombres().concat(", " + empleadoPrestamo.getApellidos())
				+ " (código empleado " + empleadoPrestamo.getId() + ") al usuario: "
				+ prestamo.getUsuario().getNombres() + " (código usuario " + prestamo.getUsuario().getId() + ")");
		// DEVOLUCION
		prestamo.setDevolucion(false);
		prestamoService.save(prestamo);
		flash.addFlashAttribute("success", "Orden de prestamo creada correctamente.");
		flash.addFlashAttribute("confirma", true);
		status.setComplete();
		return "redirect:/prestamos/listar";
	}

	// CONFIRMACIÓN DE ORDEN DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/confirmar-orden/{id}")
	public String confirmarOrden(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
		if (id > 0) {
			Prestamo prestamo = prestamoService.findById(id);
			if (prestamo.getEmpleado().getUsername().contains("Prueba")) {
				prestamo.setEmpleado(empleado);
				prestamo.setDevolucion(false);
				prestamo.setObservaciones("La orden del libro: " + prestamo.getLibro().getTitulo()
						+ ", ha sido confirmada por el empleado: "
						+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
						+ empleado.getId() + ")");
				prestamoService.save(prestamo);
				flash.addFlashAttribute("info", "La orden del libro '" + prestamo.getLibro().getTitulo()
						+ "' ha sido confirmada (còdigo " + prestamo.getId() + ").");
				flash.addFlashAttribute("confirma", true);
			} else {
				flash.addFlashAttribute("error", "Esta orden de prèstamo ya està asignada a otro empleado!");
				// return "redirect:/prestamos/listar";
			}
		}
		return "redirect:/prestamos/listar";
	}

	// CONFIRMACIÓN DE DEVOLUCIÒN DE LIBRO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/devolver-libro/{id}")
	public String devolverLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
		if (id > 0) {
			Prestamo prestamo = prestamoService.findById(id);
			// EMPLEADO
			prestamo.setEmpleado(empleado);
			// ESTADO DEVOLUCION
			prestamo.setDevolucion(true);
			// ACTUALIZACIÓN DE STOCK
			int stockNuevo = prestamo.getLibro().getStock();
			Libro libro;
			try {
				libro = libroService.findOne(prestamo.getLibro().getId());
				libro.setStock(stockNuevo + 1);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				// return "redirect:/prestamos/listar";
			}
			// FECHA DEVOLUCION
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo() + ", ha sido devuelto el día "
					+ prestamo.getFecha_devolucion() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("info", "El libro '" + prestamo.getLibro().getTitulo() + "' ha sido devuelto.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/listar";
	}

	// ANULACIÒN DE PRÉSTAMO, AUN PRESENTE EN LA BD
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/anular-prestamo/{id}")
	public String anularPrestamo(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
		if (id > 0) {
			prestamo = prestamoService.findById(id); // EMPLEADO
			prestamo.setEmpleado(empleado); // ESTADO DEVOLUCION
			prestamo.setDevolucion(true); // ACTUALIZACIÓN DE STOCK
			int stockNuevo = prestamo.getLibro().getStock();
			Libro libro;
			try {
				libro = libroService.findOne(prestamo.getLibro().getId());
				libro.setStock(stockNuevo + 1);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/prestamos/listar";
			} // FECHA DEVOLUCION
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El préstamo del libro: " + prestamo.getLibro().getTitulo() + " (código " + id
					+ "), ha sido anulado el día " + prestamo.getFecha_devolucion() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("warning",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/listar";
	}

	// ANULACIÒN DE PRÉSTAMO, ELIMINACION DE LA BD
	/*
	 * @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	 * 
	 * @GetMapping(value = "/anular-prestamo/{id}") public String
	 * anularPrestamoDelete(@PathVariable(value = "id") Long id, RedirectAttributes
	 * flash, Model model) { if (id > 0) { Prestamo prestamo =
	 * prestamoService.findById(id); // ACTUALIZACIÓN DE STOCK int stockNuevo =
	 * prestamo.getLibro().getStock(); Libro libro; try { libro =
	 * libroService.findOne(prestamo.getLibro().getId()); libro.setStock(stockNuevo
	 * + 1); } catch (Exception e) { model.addAttribute("error", e.getMessage()); //
	 * return "redirect:/prestamos/listar"; } flash.addFlashAttribute("warning",
	 * "El préstamo del libro '" + prestamo.getLibro().getTitulo() +
	 * "' ha sido anulado."); prestamoService.delete(id);
	 * flash.addFlashAttribute("confirma", true); } return
	 * "redirect:/prestamos/listar"; }
	 */

	// ############################ USUARIO ############################
	// HISTORIAL DE PRESTAMOS DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/historial-user")
	public String listarHistorialUser(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Historial de Préstamos");
		// VE PRESTAMOS A SU NOMBRE (HISTORIAL)
		model.addAttribute("prestamos",
				prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(usuario.getId()));
		return "/prestamos/listar";
	}

	// HISTORIAL DE PRESTAMOS DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/prestamos-pendientes")
	public String listarOrdenesPendientes(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Préstamos Pendientes");
		// VE PRESTAMOS PENDIENTES (HISTORIAL)
		model.addAttribute("prestamosPendientes",
				prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(usuario.getId()));
		return "/prestamos/prestamos-pendientes";
	}

	// ANULACIÒN DE PRÉSTAMO POR PARTE DEL USUARIO
	/*@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/anular-orden/{id}")
	public String anularPrestamoUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		if (id > 0) {
			Prestamo prestamo = prestamoService.findById(id);
			// ACTUALIZACIÓN DE STOCK
			int stockNuevo = prestamo.getLibro().getStock();
			try {
				Libro libro = libroService.findOne(prestamo.getLibro().getId());
				libro.setStock(stockNuevo + 1);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/prestamos/prestamos-pendientes";
			}
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
			prestamo.setUsuario(usuario);
			prestamo.setDevolucion(true);
			prestamo.setObservaciones("El préstamo del libro: " + prestamo.getLibro().getTitulo() + " (código " + id
					+ "), ha sido anulado el día " + prestamo.getFecha_devolucion() + ", por el usuario: "
					+ prestamo.getUsuario().getNombres().concat(", " + prestamo.getUsuario().getApellidos())
					+ " (código usuario " + prestamo.getUsuario().getId() + ")");
			// prestamoService.delete(id);
			prestamoService.save(prestamo);
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(usuario.getId()));
			flash.addFlashAttribute("error",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/historial-user";
	}*/

	// ANULACIÒN DE PRÉSTAMO POR PARTE DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/anular-orden/{id}")
	public String anularPrestamoUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
		if (id > 0) {
			prestamo = prestamoService.findById(id); 
			prestamo.setDevolucion(true); 
			// ACTUALIZACIÓN DE STOCK
			int stockNuevo = prestamo.getLibro().getStock();
			Libro libro;
			try {
				libro = libroService.findOne(prestamo.getLibro().getId());
				libro.setStock(stockNuevo + 1);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/prestamos/listar";
			} 
			// FECHA DEVOLUCION
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El préstamo del libro: " + prestamo.getLibro().getTitulo() + " (código " + id
					+ "), ha sido anulado el día " + prestamo.getFecha_devolucion() + ", por el usuario: "
					+ prestamo.getUsuario().getNombres().concat(", " + prestamo.getUsuario().getApellidos())
					+ " (código usuario " + prestamo.getUsuario().getId() + ")");
			prestamoService.save(prestamo);
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(usuario.getId()));
			flash.addFlashAttribute("error",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/historial-user";
	}
}
