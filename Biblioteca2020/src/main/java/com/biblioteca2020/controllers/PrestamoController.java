package com.biblioteca2020.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	// FILTRO DE LISTADO POR ROLES:
	// EMPLEADO: VE PRESTAMOS REALIZADOS A SU NOMBRE
	// SUPERVISOR Y ADMIN: VEN PRESTAMOS DEL LOCAL ENTERO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/listar")
	public String listarPrestamosPorRol(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		switch (userDetails.getAuthorities().toString()) {
		case "[ROLE_ADMIN]":
			model.addAttribute("prestamos", prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado());
			break;
		case "[ROLE_SUPERVISOR]":
			model.addAttribute("prestamos", prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado());
			break;
		case "[ROLE_EMPLEADO]":
			Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
			model.addAttribute("prestamos",
					prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(empleado.getId()));
			break;
		}

		model.addAttribute("titulo", "Listado de Préstamos");
		return "/prestamos/listar";
	}

	// HISTORIAL DE PRESTAMOS DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/historialUser")
	public String listarHistorialUser(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Historial de Préstamos");
		model.addAttribute("prestamos",
				prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(usuario.getId()));
		return "/prestamos/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping(value = "/cancelar")
	public String cancelar() {
		return "redirect:/prestamos/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE LIBROS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarLibros/{term}", produces = { "application/json" })
	public @ResponseBody List<Libro> cargarLibros(@PathVariable String term) {
		return libroService.findByTituloLikeIgnoreCaseAndEstado(term, true);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE USUARIOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarUsuarios/{term}", produces = { "application/json" })
	public @ResponseBody List<Usuario> cargarUsuarios(@PathVariable String term) {
		return usuarioService.findAllByNroDocumentoAndEstado(term, true);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE EMPLEADOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarEmpleados/{term}", produces = { "application/json" })
	public @ResponseBody List<Empleado> cargarEmpleados(@PathVariable String term) {
		return empleadoService.findAllByNroDocumento(term);
	}

	// CARGA DE FORMULARIO DE CREACIÓN DE PRÉSTAMOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
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
	 // 1: EL LIBRO SE BUSCA ENTRE LOS EXISTENTES (AJAX), 
	 //    OBTENGO NOMBRE, AUTOR Y SE ALMACENA SU CODIGO;
	 // 2: EL SUSCRIPTOR SE BUSCA ENTRE LOS EXISTENTES (AJAX),
	 //    OBTENGO NOMBRES, APELLIDOS Y SE ALMACENA EN SU CÓDIGO;
	 // 3: LA FECHA DE DEVOLUCIÒN SE SETEA CON LA FECHA ACTUAL; 
	 // 4: EL EMPLEADO QUE REGISTRA EL PRÉSTAMO SE SETEA CON EL EMPLEADO LOGUEADO EN EL SISTEMA
	 //    Y SE ALMACENA SU CODIGO;	 
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@PostMapping(value = "/crear")
	public String guardarPrestamo(Prestamo prestamo, @RequestParam(name = "id_libro", required = false) Long id_libro,
			@RequestParam(name = "id_usuario", required = false) Long id_usuario,
			@RequestParam(name = "fecha_devolucion", required = false) String fecha_devolucion,
			RedirectAttributes flash, SessionStatus status, Model model, Authentication authentication) {

		if (id_libro == null || id_usuario == null || fecha_devolucion == null) {
			model.addAttribute("titulo", "Creación de Préstamo");
			model.addAttribute("error", "El prestamo necesita un libro, un usuario y una fecha de despacho VÁLIDOS.");
			return "/prestamos/crear";
		}

		// LIBRO
		Libro libroPrestamo;

		try {
			libroPrestamo = libroService.findOne(id_libro);
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
			usuarioPrestamo = usuarioService.findById(id_usuario);
			prestamo.setUsuario(usuarioPrestamo);
		} catch (Exception e1) {
			model.addAttribute("error", e1.getMessage());
			return "/prestamos/crear";
		}

		// EMPLEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		prestamo.setEmpleado(empleadoPrestamo);

		// FECHA DESPACHO
		Date fechaDespacho = new Date();
		prestamo.setFecha_despacho(fechaDespacho);

		// FECHA DEVOLUCIÓN
		Date fechaDevolucionPrestamo;

		try {
			fechaDevolucionPrestamo = new SimpleDateFormat("yyyy-mm-dd").parse(fecha_devolucion);
			prestamo.setFecha_devolucion(fechaDevolucionPrestamo);
		} catch (ParseException pe) {
			model.addAttribute("error", pe.getMessage());
			return "/prestamos/crear";
		}

		// OBSERVACIONES
		prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo()
				+ " ha sido programado para su devolución el día " + prestamoService.mostrarFechaAmigable()
				+ ", por el empleado: " + empleadoPrestamo.getNombres().concat(", " + empleadoPrestamo.getApellidos())
				+ " (código empleado " + empleadoPrestamo.getId() + ") al usuario: " + usuarioPrestamo.getNombres()
				+ " (código usuario " + usuarioPrestamo.getId() + ")");

		// DEVOLUCION
		prestamo.setDevolucion(false);

		prestamoService.save(prestamo);
		flash.addFlashAttribute("success", "Orden de prestamo creada correctamente.");
		status.setComplete();
		return "redirect:/prestamos/listar";
	}

	// CONFIRMACIÓN DE DEVOLUCIÒN DE LIBRO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/devolverLibro/{id}")
	public String devolverLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername().toString());
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
		}
		return "redirect:/prestamos/listar";
	}

	// ANULACIÒN DE PRÉSTAMO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/anularPrestamo/{id}")
	public String anularPrestamo(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication, Model model) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername().toString());
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
		}
		return "redirect:/prestamos/listar";
	}
}
