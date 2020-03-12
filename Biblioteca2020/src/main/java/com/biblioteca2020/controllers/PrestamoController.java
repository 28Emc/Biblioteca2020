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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
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

	@GetMapping(value = "/listar")
	public String listarPrestamos(Model model) {
		model.addAttribute("titulo", "Listado de Préstamos");
		model.addAttribute("prestamos", prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado());
		//System.out.println(prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado().toString());
		return "/prestamos/listar";
	}

	@GetMapping(value = "/cancelar")
	public String cancelar(Model model) {
		model.addAttribute("titulo", "Listado de Préstamos");
		model.addAttribute("prestamos", prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado());
		return "redirect:/prestamos/listar";
	}

	// MÉTODO DE CONFIRMACIÓN DE DEVOLUCIÒN DEL LIBRO
	@RequestMapping(value = "/devolverLibro/{id}")
	public String devolverLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername().toString());
		if (id > 0) {
			prestamo = prestamoService.findById(id);
			prestamo.setDevolucion(true);

			/* AQUÍ FALTA AUN LA LÓGICA DE ACTUALIZACIÓN DEL STOCK (LIBRO) */

			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El libro '" + prestamo.getLibro().getTitulo() + "' ha sido devuelto el día "
					+ prestamoService.mostrarFechaAmigable() + ", por el empleado "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado: "
					+ empleado.getId() + ").");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("info", "El libro '" + prestamo.getLibro().getTitulo() + "' ha sido devuelto.");
		}
		return "redirect:/prestamos/listar";
	}

	// MÉTODO DE ANULACIÒN DE PRÉSTAMO
	@GetMapping(value = "/anularPrestamo/{id}")
	public String anularPrestamo(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication) {
		Prestamo prestamo = null;
		Date fechaDevolución = new Date();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername().toString());
		if (id > 0) {
			prestamo = prestamoService.findById(id);
			prestamo.setDevolucion(true);

			/* AQUÍ FALTA AUN LA LÓGICA DE ACTUALIZACIÓN DEL STOCK (LIBRO) */

			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' con código ("
					+ id + ") ha sido anulado el día " + prestamoService.mostrarFechaAmigable() + ", por el empleado "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado: "
					+ empleado.getId() + ").");
			prestamoService.save(prestamo);
			flash.addFlashAttribute("warning",
					"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
		}
		return "redirect:/prestamos/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE LIBROS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarLibros/{term}", produces = { "application/json" })
	public @ResponseBody List<Libro> cargarLibros(@PathVariable String term) {
		return prestamoService.findByTitulo(term);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE USUARIOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarUsuarios/{term}", produces = { "application/json" })
	public @ResponseBody List<Usuario> cargarUsuarios(@PathVariable String term) {
		return usuarioService.findAllByNroDocumento(term);
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE EMPLEADOS MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargarEmpleados/{term}", produces = { "application/json" })
	public @ResponseBody List<Empleado> cargarEmpleados(@PathVariable String term) {
		return empleadoService.findAllByNroDocumento(term);
	}

	@GetMapping(value = "/crear")
	public String crearPrestamo(Map<String, Object> model, Authentication authentication) {
		Prestamo prestamo = new Prestamo();
		model.put("titulo", "Creación de Prestamo");
		model.put("prestamo", prestamo);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoPrestamo = empleadoService.findByUsernameAndEstado(userDetails.getUsername(), true);
		model.put("empleado", empleadoPrestamo);
		return "/prestamos/crear";
	}

	/*
	 * LA LOGICA DE REGISTRO DE prestamo: 1- EL LIBRO SE BUSCA ENTRE LOS EXISTENTES
	 * (AJAX), DEVUELVE SU NOMBRE Y SU AUTOR, SE ALMACENA SU CODIGO; 2- EL
	 * SUSCRIPTOR SE BUSCA ENTRE LOS EXISTENTES (AJAX), DEVUELVE EL NOMBRE Y
	 * APELLIDO, SE ALMACENA EN EL prestamo SU CODIGO; 3- LA FECHA DE DEVOLUCIÒN SE
	 * SETEA CON LA FECHA ACTUAL; 4- EL EMPLEADO QUE ATIENDE SE SETEA CON EL
	 * EMPLEADO LOGUEADO EN ESE MOMENTO EN EL SISTEMA, SE ALMACENA SU CODIGO;
	 */
	@PostMapping(value = "/crear")
	public String guardarPrestamo(Prestamo prestamo,
			@RequestParam(name = "id_libro", required = false) Long id_libro,
			@RequestParam(name = "id_usuario", required = false) Long id_usuario,
			@RequestParam(name = "fecha_devolucion", required = false) String fecha_devolucion,
			RedirectAttributes flash, SessionStatus status, Model model, Authentication authentication) {

		if (id_libro == null || id_usuario == null || fecha_devolucion == null) {
			model.addAttribute("titulo", "Creación de Prestamo");
			// model.addAttribute("empleado", empleadoPrestamo);
			model.addAttribute("error", "El prestamo necesita un libro, un usuario y una fecha de despacho VÁLIDOS.");
			return "/prestamos/crear";
		}

		// LIBRO
		Libro libroPrestamo = libroService.findOne(id_libro);
		prestamo.setLibro(libroPrestamo);

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
		prestamo.setObservaciones("No hay observaciones.");

		// DEVOLUCION
		prestamo.setDevolucion(false);

		/* AQUÍ FALTA AUN LA LÓGICA DE ACTUALIZACIÓN DEL STOCK (LIBRO) */

		prestamoService.save(prestamo);
		flash.addFlashAttribute("success", "El prestamo ha sido creado correctamente.");
		status.setComplete();
		return "redirect:/prestamos/listar";
	}
}
