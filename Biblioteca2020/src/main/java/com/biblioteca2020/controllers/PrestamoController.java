package com.biblioteca2020.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
		// MOSTRAR LISTADO DE ACUERDO A ROL
		switch (userDetails.getAuthorities().toString()) {
		// ADMIN: VE PRESTAMOS DEL LOCAL ENTERO
		case "[ROLE_ADMIN]":
			model.addAttribute("prestamos", prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado());
			break;
		// EMPLEADO: VE PRESTAMOS REALIZADOS A SU NOMBRE
		case "[ROLE_EMPLEADO]":
			Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
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
	public String crearPrestamo(Map<String, Object> model, Authentication authentication) {
		Prestamo prestamo = new Prestamo();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		model.put("titulo", "Creación de Préstamo");
		model.put("prestamo", prestamo);
		model.put("empleado", empleadoPrestamo);
		return "/prestamos/crear";
	}

	// REGISTRO DE PRÉSTAMOS:
	// 1: EL LIBRO SE BUSCA ENTRE LOS EXISTENTES (AJAX - AUTOCOMPLETADO),
	// OBTENGO NOMBRE, AUTOR Y SE ALMACENA SU CÓDIGO;
	// 2: EL SUSCRIPTOR SE BUSCA ENTRE LOS EXISTENTES (AJAX),
	// OBTENGO NOMBRES, APELLIDOS Y SE ALMACENA EN SU CÓDIGO;
	// 3: LA FECHA DE DEVOLUCIÒN SE SETEA CON LA FECHA ACTUAL;
	// 4: EL EMPLEADO QUE REGISTRA EL PRÉSTAMO SE SETEA CON EL EMPLEADO LOGUEADO EN
	// EL SISTEMA
	// Y SE ALMACENA SU CODIGO;
	/*
	 * ACTUALIZACIÓN 07/04/2020 AHORA YA PUEDO USAR @VALID PARA CREAR ORDEN DE
	 * PRESTAMOS.
	 */
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
		Libro libroPrestamo;
		try {
			libroPrestamo = libroService.findOne(/* id_libro */prestamo.getLibro().getId());
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
		Usuario usuarioPrestamo;
		try {
			usuarioPrestamo = usuarioService.findById(/* id_usuario */prestamo.getUsuario().getId());
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
				+ " ha sido programado para su devolución el día " + fechaPrestamoHoy + ", por el empleado: "
				+ empleadoPrestamo.getNombres().concat(", " + empleadoPrestamo.getApellidos()) + " (código empleado "
				+ empleadoPrestamo.getId() + ") al usuario: " + usuarioPrestamo.getNombres() + " (código usuario "
				+ usuarioPrestamo.getId() + ")");
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
		Prestamo prestamo = null;
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
		if (id > 0) {
			prestamo = prestamoService.findById(id);
			// ACTUALIZO EL ID EMPLEADO CON EL LOGUEADO, ANTES DE ESO, TENGO QUE ASEGURARME
			// QUE ESTE USUARIO SEA EL DEL EMPLEADO DE PRUEBAS PARA NO "ROBAR" EL ORDEN DE
			// OTRO EMPLEADO
			if (prestamo.getEmpleado().getUsername().equals("Prueba")) {
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
				flash.addFlashAttribute("error", "Esta orden de prèstamo ya està asignada a otro empleado");
				return "redirect:/prestamos/listar";
			}
		}
		return "redirect:/prestamos/listar";
	}

	// CONFIRMACIÓN DE DEVOLUCIÒN DE LIBRO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/devolver-libro/{id}")
	public String devolverLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
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
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo() + ", ha sido devuelto el día "
					+ prestamoService.mostrarFechaAmigable() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("info", "El libro '" + prestamo.getLibro().getTitulo() + "' ha sido devuelto.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/listar";
	}

	// ANULACIÒN DE PRÉSTAMO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/anular-prestamo/{id}")
	public String anularPrestamo(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
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
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El préstamo del libro: " + prestamo.getLibro().getTitulo() + " (código " + id
					+ "), ha sido anulado el día " + prestamoService.mostrarFechaAmigable() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("warning",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/listar";
	}

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
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/anular-orden/{id}")
	public String anularPrestamoUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		if (id > 0) {
			prestamo = prestamoService.findById(id);
			// ACTUALIZACIÓN DE STOCK
			int stockNuevo = prestamo.getLibro().getStock();
			Libro libro;

			try {
				libro = libroService.findOne(prestamo.getLibro().getId());
				libro.setStock(stockNuevo + 1);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/prestamos/prestamos-pendientes";
			}
			prestamoService.delete(id);
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername().toString(), true);
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(usuario.getId()));
			flash.addFlashAttribute("error",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
			flash.addFlashAttribute("confirma", true);
		}
		return "redirect:/prestamos/historial-user";
	}
}
