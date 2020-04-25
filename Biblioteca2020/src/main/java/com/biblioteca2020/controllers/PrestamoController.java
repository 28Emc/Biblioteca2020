package com.biblioteca2020.controllers;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
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
import net.sf.jasperreports.engine.JRException;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.PrestamoLog;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.EmailSenderService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.IPrestamoLogService;
import com.biblioteca2020.models.service.IPrestamoService;
import com.biblioteca2020.models.service.IUsuarioService;
import com.biblioteca2020.models.service.ReporteService;

@Controller
@RequestMapping("/prestamos")
@SessionAttributes("prestamos")
public class PrestamoController {

	@Autowired
	private IPrestamoService prestamoService;

	@Autowired
	private IPrestamoLogService prestamoLogService;

	@Autowired
	private ILibroService libroService;

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private EmailSenderService emailSenderService;

	@Autowired
	private ReporteService reporteService;

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

	@GetMapping("/reportes/{formato}")
	public String generarReportePrestamos(@PathVariable String formato, String role, Authentication authentication,
			RedirectAttributes flash) {
		try {
			// OBTENER USUARIO LOGUEADO ACTUALMENTE
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			role = userDetails.getAuthorities().toString();
			switch (role) {
				case "[ROLE_ADMIN]":
					if (formato.equalsIgnoreCase("pdf")) {
						flash.addFlashAttribute("success",
								reporteService.exportarReportePrestamos(formato, role, authentication));
					}
					if (formato.equalsIgnoreCase("xlsx")) {
						flash.addFlashAttribute("success",
								reporteService.exportarReportePrestamos(formato, role, authentication));
					}
					break;
				case "[ROLE_EMPLEADO]":
					if (formato.equalsIgnoreCase("pdf")) {
						flash.addFlashAttribute("success",
								reporteService.exportarReportePrestamos(formato, role, authentication));
					}
					if (formato.equalsIgnoreCase("xlsx")) {
						flash.addFlashAttribute("success",
								reporteService.exportarReportePrestamos(formato, role, authentication));
					}
					break;
			}
		} catch (FileNotFoundException | JRException e) {
			flash.addFlashAttribute("error", "Lo sentimos, hubo un error a la hora de generar el archivo pdf. Inténtalo mas tarde.");
		}
		return "redirect:/prestamos/listar";
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
		// OBSERVACIONES
		prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo()
				+ " ha sido programado para su devolución el día " + prestamo.getFecha_devolucion()
				+ ", por el empleado: " + empleadoPrestamo.getNombres().concat(", " + empleadoPrestamo.getApellidos())
				+ " (código empleado " + empleadoPrestamo.getId() + ") al usuario: "
				+ prestamo.getUsuario().getNombres() + " (código usuario " + prestamo.getUsuario().getId() + ")");
		// DEVOLUCION
		prestamo.setDevolucion(false);
		prestamoService.save(prestamo);

		// JUSTO DESPUES DE REGISTRAR EL PRÉSTAMO, INSERTO EL REGISTRO EN MI TABLA LOG
		prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamo.getEmpleado().getId(), null,
				prestamo.getLibro().getId(), null, prestamo.getUsuario().getId(), null, "INSERT EMPLOYEE",
				empleadoPrestamo.getUsername().concat(" (Cod. Empleado: " + empleadoPrestamo.getId() + ")"),
				prestamo.getFecha_despacho(), null, prestamo.getFecha_devolucion(), null, prestamo.getDevolucion(),
				null, prestamo.getObservaciones(), null, new Date(), null, null));

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
			// ME SIRVEN LOS DATOS DEL PRESTAMO ANTES DE SER ACTUALIZADOS
			Prestamo prestamoOld = prestamo;
			if (prestamo.getEmpleado().getUsername().contains("Prueba")) {
				prestamo.setEmpleado(empleado);
				prestamo.setDevolucion(false);
				prestamo.setObservaciones("La orden del libro: " + prestamo.getLibro().getTitulo()
						+ ", ha sido confirmada por el empleado: "
						+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
						+ empleado.getId() + ")");
				prestamoService.save(prestamo);

				// JUSTO DESPUES DE CONFIRMAR LA ORDEN DE PRÉSTAMO, INSERTO EL REGISTRO EN MI
				// TABLA LOG
				prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamoOld.getEmpleado().getId(),
						prestamo.getEmpleado().getId(), prestamoOld.getLibro().getId(), prestamo.getLibro().getId(),
						prestamoOld.getUsuario().getId(), prestamo.getUsuario().getId(), "CONFIRMA ORDEN EMPLOYEE",
						prestamo.getEmpleado().getUsername()
								.concat(" (Cod. Empleado: " + prestamo.getEmpleado().getId() + ")"),
						prestamoOld.getFecha_despacho(), prestamo.getFecha_despacho(),
						prestamoOld.getFecha_devolucion(), prestamo.getFecha_devolucion(), prestamoOld.getDevolucion(),
						prestamo.getDevolucion(), prestamoOld.getObservaciones(), prestamo.getObservaciones(), null,
						new Date(), null));

				flash.addFlashAttribute("info", "La orden del libro '" + prestamo.getLibro().getTitulo()
						+ "' ha sido confirmada (còdigo " + prestamo.getId() + ").");
				flash.addFlashAttribute("confirma", true);
			} else {
				flash.addFlashAttribute("error", "Esta orden de prèstamo ya està asignada a otro empleado!");
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
			// ME SIRVEN LOS DATOS DEL PRESTAMO ANTES DE SER ACTUALIZADOS
			Prestamo prestamoOld = prestamo;
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
			}
			// FECHA DEVOLUCION
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El libro: " + prestamo.getLibro().getTitulo() + ", ha sido devuelto el día "
					+ prestamo.getFecha_devolucion() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");
			prestamoService.save(prestamo);

			// JUSTO DESPUES DE DEVOLVER EL LIBRO, INSERTO EL REGISTRO EN MI
			// TABLA LOG
			prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamoOld.getEmpleado().getId(),
					prestamo.getEmpleado().getId(), prestamoOld.getLibro().getId(), prestamo.getLibro().getId(),
					prestamoOld.getUsuario().getId(), prestamo.getUsuario().getId(), "DEVOLVER LIBRO EMPLOYEE",
					prestamo.getEmpleado().getUsername()
							.concat(" (Cod. Empleado: " + prestamo.getEmpleado().getId() + ")"),
					prestamoOld.getFecha_despacho(), prestamo.getFecha_despacho(), prestamoOld.getFecha_devolucion(),
					prestamo.getFecha_devolucion(), prestamoOld.getDevolucion(), prestamo.getDevolucion(),
					prestamoOld.getObservaciones(), prestamo.getObservaciones(), null, new Date(), null));

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
			prestamo = prestamoService.findById(id);
			Prestamo prestamoOld = prestamo;

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
				return "redirect:/prestamos/listar";
			} // FECHA DEVOLUCION
			prestamo.setFecha_devolucion(fechaDevolución);
			prestamo.setObservaciones("El préstamo del libro: " + prestamo.getLibro().getTitulo() + " (código " + id
					+ "), ha sido anulado el día " + prestamo.getFecha_devolucion() + ", por el empleado: "
					+ empleado.getNombres().concat(", " + empleado.getApellidos()) + " (código empleado "
					+ empleado.getId() + ")");

			// ENVIO DE MAIL DE CONFIRMACIÓN DE ANULACION AL USUARIO CON MIMEMESSAGE
			try {
				String message = "<html><head>" + "<meta charset='UTF-8' />"
						+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
						+ "<title>Préstamo Anulado | Biblioteca2020</title>" + "</head>" + "<body>"
						+ "<div class='container' style='padding-top: 1rem;'>"
						+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
						+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos "
						+ prestamo.getUsuario().getUsername()
						+ ", se procedió a anular su orden de préstamo del libro '" + prestamo.getLibro().getTitulo()
						+ "', con Código Préstamo '" + prestamo.getId() + "' situado en el local '"
						+ prestamo.getLibro().getLocal().getDireccion() + "'.</p><br/>"
						+ "<p>Si usted no estaba al corriente de dicha acción, favor de notificarlo al local donde realizó la orden.</p><br/>"
						+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
						+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
						+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
						+ "</body>"
						+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
						+ "</html>";
				emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", prestamo.getUsuario().getEmail(),
						"Préstamo Anulado | Biblioteca2020", message);

				prestamoService.save(prestamo);

				// JUSTO DESPUES DE DEVOLVER EL LIBRO, INSERTO EL REGISTRO EN MI
				// TABLA LOG
				prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamoOld.getEmpleado().getId(),
						prestamo.getEmpleado().getId(), prestamoOld.getLibro().getId(), prestamo.getLibro().getId(),
						prestamoOld.getUsuario().getId(), prestamo.getUsuario().getId(), "ANULAR PRESTAMO EMPLOYEE",
						prestamo.getEmpleado().getUsername()
								.concat(" (Cod. Empleado: " + prestamo.getEmpleado().getId() + ")"),
						prestamoOld.getFecha_despacho(), prestamo.getFecha_despacho(),
						prestamoOld.getFecha_devolucion(), prestamo.getFecha_devolucion(), prestamoOld.getDevolucion(),
						prestamo.getDevolucion(), prestamoOld.getObservaciones(), prestamo.getObservaciones(), null,
						new Date(), null));

				flash.addFlashAttribute("warning",
						"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
				flash.addFlashAttribute("confirma", true);
			} catch (MailException ex) {
				model.addAttribute("error", ex.getMessage());
			}
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
	/*
	 * @PreAuthorize("hasAnyRole('ROLE_USER')")
	 * 
	 * @GetMapping(value = "/anular-orden/{id}") public String
	 * anularPrestamoUsuario(@PathVariable(value = "id") Long id, RedirectAttributes
	 * flash, Authentication authentication, Model model) { if (id > 0) { Prestamo
	 * prestamo = prestamoService.findById(id); // ACTUALIZACIÓN DE STOCK int
	 * stockNuevo = prestamo.getLibro().getStock(); try { Libro libro =
	 * libroService.findOne(prestamo.getLibro().getId()); libro.setStock(stockNuevo
	 * + 1); } catch (Exception e) { model.addAttribute("error", e.getMessage());
	 * return "redirect:/prestamos/prestamos-pendientes"; } UserDetails userDetails
	 * = (UserDetails) authentication.getPrincipal(); Usuario usuario =
	 * usuarioService.findByUsernameAndEstado(userDetails.getUsername().toString(),
	 * true); prestamo.setUsuario(usuario); prestamo.setDevolucion(true);
	 * prestamo.setObservaciones("El préstamo del libro: " +
	 * prestamo.getLibro().getTitulo() + " (código " + id +
	 * "), ha sido anulado el día " + prestamo.getFecha_devolucion() +
	 * ", por el usuario: " + prestamo.getUsuario().getNombres().concat(", " +
	 * prestamo.getUsuario().getApellidos()) + " (código usuario " +
	 * prestamo.getUsuario().getId() + ")"); // prestamoService.delete(id);
	 * prestamoService.save(prestamo); model.addAttribute("prestamos",
	 * prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(
	 * usuario.getId())); flash.addFlashAttribute("error", "El préstamo del libro '"
	 * + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
	 * flash.addFlashAttribute("confirma", true); } return
	 * "redirect:/prestamos/historial-user"; }
	 */

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
			Prestamo prestamoOld = prestamo;

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

			// ENVIO DE MAIL DE CONFIRMACIÓN DE ANULACION AL USUARIO CON MIMEMESSAGE
			try {
				String message = "<html><head>" + "<meta charset='UTF-8' />"
						+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
						+ "<title>Préstamo Anulado | Biblioteca2020</title>" + "</head>" + "<body>"
						+ "<div class='container' style='padding-top: 1rem;'>"
						+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
						+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
						+ ", le comunicamos que su orden de préstamo del libro '" + prestamo.getLibro().getTitulo()
						+ "', con Código Préstamo '" + prestamo.getId() + "' y situado en el local con dirección '"
						+ prestamo.getLibro().getLocal().getDireccion() + "' ha sido anulada.</p><br/>"
						+ "<p>Si usted no estaba al corriente de dicha acción, favor de notificarlo al local donde realizó la orden.</p><br/>"
						+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
						+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
						+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
						+ "</body>"
						+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
						+ "</html>";
				emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", prestamo.getUsuario().getEmail(),
						"Préstamo Anulado | Biblioteca2020", message);

				prestamoService.save(prestamo);

				// JUSTO DESPUES DE ANULAR EL PRÉSTAMO, INSERTO EL REGISTRO EN MI
				// TABLA LOG
				prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamoOld.getEmpleado().getId(),
						prestamo.getEmpleado().getId(), prestamoOld.getLibro().getId(), prestamo.getLibro().getId(),
						prestamoOld.getUsuario().getId(), prestamo.getUsuario().getId(), "ANULAR PRESTAMO USER",
						prestamo.getUsuario().getUsername()
								.concat(" (Cod. Usuario: " + prestamo.getUsuario().getId() + ")"),
						prestamoOld.getFecha_despacho(), prestamo.getFecha_despacho(),
						prestamoOld.getFecha_devolucion(), prestamo.getFecha_devolucion(), prestamoOld.getDevolucion(),
						prestamo.getDevolucion(), prestamoOld.getObservaciones(), prestamo.getObservaciones(), null,
						new Date(), null));

				model.addAttribute("prestamos",
						prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(usuario.getId()));
				flash.addFlashAttribute("error",
						"El préstamo del libro '" + prestamo.getLibro().getTitulo() + "' ha sido anulado.");
				flash.addFlashAttribute("confirma", true);
			} catch (MailException ex) {
				model.addAttribute("error", ex.getMessage());
			}
		}
		return "redirect:/prestamos/historial-user";
	}
}
