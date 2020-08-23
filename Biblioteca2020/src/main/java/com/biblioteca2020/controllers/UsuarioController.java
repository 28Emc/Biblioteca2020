package com.biblioteca2020.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.biblioteca2020.models.dao.IConfirmationTokenDao;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.dto.RecuperarCuenta;
import com.biblioteca2020.models.entity.ConfirmationToken;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.PrestamoLog;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.entity.UsuarioLog;
import com.biblioteca2020.models.service.EmailSenderService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.IPrestamoLogService;
import com.biblioteca2020.models.service.IPrestamoService;
import com.biblioteca2020.models.service.IRoleService;
import com.biblioteca2020.models.service.IUsuarioLogService;
import com.biblioteca2020.models.service.IUsuarioService;
import com.biblioteca2020.view.pdf.GenerarReportePDF;
import com.biblioteca2020.view.xlsx.GenerarReporteExcel;

@Controller
@RequestMapping("/usuarios")
@SessionAttributes("usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IUsuarioLogService usuarioLogService;

	@Autowired
	private IPrestamoService prestamoService;

	@Autowired
	private IPrestamoLogService prestamoLogService;

	@Autowired
	private IConfirmationTokenDao confirmationTokenRepository;

	@Autowired
	private EmailSenderService emailSenderService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private ILibroService libroService;

	@Autowired
	private IEmpleadoService empleadoService;

	// LISTADO DE FORMATOS DE FOTO PERMITIDOS
	private static final List<String> formatosFoto = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	// ################ LÓGICA DEL LADO DEL USUARIO ################
	// LISTADO DE LIBROS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca")
	public String listarAllLibrosUser(Model model, Principal principal) {
		model.addAttribute("titulo", "Catálogo de libros");
		// ESTE MÈTODO ES DE REFERENCIA NADA MAS, A LA HORA DE VER LA DISPONIBILIDAD,
		// AHI SI FILTRO LOS LIBROS POR LOCAL
		List<Libro> libros = libroService.findByTituloGroup();
		// LÓGICA DE MOSTRAR UNA DESCRICIÓN REDUCIDA DE 150 CARACTERES DEL LIBRO
		for (int i = 0; i < libros.size(); i++) {
			String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
			String descripcionFull = libros.get(i).getDescripcion().substring(150,
					libros.get(i).getDescripcion().length());
			libros.get(i).setDescripcionMin(descripcionMin + " ...");
			libros.get(i).setDescripcion(descripcionFull);
			model.addAttribute("libros", libros);
		}
		return "/usuarios/biblioteca";
	}

	// FORMULARIO DETALLE DE LIBRO SELECCIONADO POR EL USUARIO SEGUN SU TITULO Y
	// DISPONIBILIDAD
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca/ver/{titulo}")
	public String verLibro(@PathVariable("titulo") String titulo, Model model, Authentication authentication) {
		List<Libro> libros = libroService.findByTituloLikeIgnoreCase(titulo);
		model.addAttribute("titulo", "Ver disponibilidad");
		model.addAttribute("libros", libros);
		// RECOJO LOS DETALLES DEL PRIMER LIBRO, YA QUE AL FINAL TODOS SON IGUALES
		model.addAttribute("libroDetalle", libros.get(0));
		return "/usuarios/biblioteca/ver";
	}

	// FORMULARIO DE REGISTRO DE ORDEN DE PRÉSTAMO POR PARTE DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca/solicitar-libro/{id}/{titulo}")
	public String solicitarLibroForm(@PathVariable("titulo") String titulo, @PathVariable("id") Long id_local,
			Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		// VALIDO SI EL LIBRO SELECCIONADO POR EL USUARIO TIENE DISPONIBILIDAD
		Libro libro = libroService.findByTituloAndLocalAndEstado(titulo, id_local, true);
		if (libro == null) {
			flash.addFlashAttribute("error",
					"Lo sentimos, el libro solicitado no está disponible en estos momentos. Inténtelo más tarde.");
			return "redirect:/usuarios/biblioteca";
		}
		model.addAttribute("titulo", "Solicitar Libro");
		model.addAttribute("libro", libro);
		model.addAttribute("titulo_libro", titulo);
		model.addAttribute("id_local", id_local);
		model.addAttribute("usuario", usuario);
		model.addAttribute("prestamo", new Prestamo());
		return "/usuarios/biblioteca/solicitar-libro";
	}

	// REGISTRAR ORDEN DE PRÉSTAMO POR PARTE DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@PostMapping(value = "/biblioteca/solicitar-libro")
	public String solicitarLibro(Prestamo prestamo, @RequestParam(name = "id_libro", required = false) Long id_libro,
			@RequestParam(name = "id_usuario", required = false) Long id_usuario,
			@RequestParam(name = "fecha_devolucion", required = false) String fecha_devolucion,
			RedirectAttributes flash, SessionStatus status, Model model, Authentication authentication) {
		try {
			if (id_libro == null || id_usuario == null || fecha_devolucion == null) {
				// model.addAttribute("prestamo", prestamo);
				// IR A BIBLIOTECA PORQUE LOS DATOS SON INCORRECTOS O NO VÀLIDOS
				model.addAttribute("titulo", "Catálogo de libros");
				List<Libro> libros = libroService.findByTituloGroup();
				for (int i = 0; i < libros.size(); i++) {
					String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
					String descripcionFull = libros.get(i).getDescripcion().substring(150,
							libros.get(i).getDescripcion().length());
					libros.get(i).setDescripcionMin(descripcionMin + " ...");
					libros.get(i).setDescripcion(descripcionFull);
					model.addAttribute("libros", libros);
				}
				model.addAttribute("error",
						"Lo sentimos, hubo un error a la hora de guardar su orden. Intentelo mas tarde.");
				return "/usuarios/biblioteca";
			}
			// ##################### LÓGICA DE REGISTRO DE ORDEN DE PRÉSTAMO
			// #####################
			// LIBRO
			Libro libroPrestamo = libroService.findByTituloAndLocalAndEstado(libroService.findOne(id_libro).getTitulo(),
					libroService.findOne(id_libro).getLocal().getId(), true);
			prestamo.setLibro(libroPrestamo);
			// ACTUALIZAR STOCK DE LIBRO
			if (libroPrestamo.getStock() == 0) {
				model.addAttribute("error", "Lo sentimos, no hay stock suficiente del libro seleccionado ("
						+ libroPrestamo.getTitulo() + ")");
				model.addAttribute("titulo", "Catálogo de libros");
				List<Libro> libros = libroService.findByTituloGroup();
				for (int i = 0; i < libros.size(); i++) {
					String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
					String descripcionFull = libros.get(i).getDescripcion().substring(150,
							libros.get(i).getDescripcion().length());
					libros.get(i).setDescripcionMin(descripcionMin + " ...");
					libros.get(i).setDescripcion(descripcionFull);
					model.addAttribute("libros", libros);
				}
				return "/usuarios/biblioteca";
			} else {
				libroPrestamo.setStock(libroPrestamo.getStock() - 1);
			}
			// USUARIO
			Usuario usuarioPrestamo = usuarioService.findById(id_usuario);
			prestamo.setUsuario(usuarioPrestamo);
			// EMPLEADO
			// EL EMPLEADO SE SETEA CON EL USUARIO DE PRUEBA DEL LOCAL DEL LIBRO POR PARTE
			// DEL USUARIO
			Empleado empleadoPrestamo = empleadoService.findByRoleAndLocal("ROLE_PRUEBA",
					libroPrestamo.getLocal().getId());
			prestamo.setEmpleado(empleadoPrestamo);
			// FECHAS
			// USO CALENDAR PARA MOSTRAR LA FECHA DE MANERA MAS AMIGABLE
			Locale locale = new Locale("es", "ES");
			Calendar calendar = Calendar.getInstance(locale);
			// FECHA_DESPACHO
			SimpleDateFormat formatterFDevolucion = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatterOut = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			String charFechaDespacho = formatterOut.format(new Date());
			prestamo.setFecha_despacho(formatterOut.parse(charFechaDespacho));
			// FECHA DEVOLUCIÓN
			Date fechaDevolucionPrestamo = formatterFDevolucion.parse(fecha_devolucion);
			prestamo.setFecha_devolucion(fechaDevolucionPrestamo);
			calendar.setTime(prestamo.getFecha_despacho());
			// MOSTRAR FECHA POR DIA, MES Y ANIO
			String anioDespacho = String.valueOf(calendar.get(Calendar.YEAR));
			String mesDespacho = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
			String diaDespacho = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			String fechaDespacho = diaDespacho + " de " + mesDespacho + " " + anioDespacho;
			calendar.setTime(prestamo.getFecha_devolucion());
			String anioDevolucion = String.valueOf(calendar.get(Calendar.YEAR));
			String mesDevolucion = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
			String diaDevolucion = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			String fechaDevolucion = diaDevolucion + " de " + mesDevolucion + " " + anioDevolucion;
			// OBSERVACIONES
			prestamo.setObservaciones("El usuario: " + prestamo.getUsuario().getNombres() + ", "
					+ prestamo.getUsuario().getApellidos() + "(DNI " + prestamo.getUsuario().getNroDocumento()
					+ ") ha solicitado el libro: " + prestamo.getLibro().getTitulo() + " el dìa " + fechaDespacho
					+ ", hasta el dìa " + fechaDevolucion + ". A la espera de confirmación.");
			// DEVOLUCION
			prestamo.setDevolucion(false);
			prestamoService.save(prestamo);
			// AL REALIZAR LA ORDEN DE PRÉSTAMO, INSERTO EL REGISTRO EN MI
			// TABLA LOG
			prestamoLogService.save(new PrestamoLog(prestamo.getId(), prestamo.getEmpleado().getId(), null,
					prestamo.getLibro().getId(), null, prestamo.getUsuario().getId(), null, "INSERT BY USER",
					prestamo.getUsuario().getUsername()
							.concat(" (Cod. Usuario: " + prestamo.getUsuario().getId() + ")"),
					prestamo.getFecha_despacho(), null, prestamo.getFecha_devolucion(), null, prestamo.getDevolucion(),
					null, prestamo.getObservaciones(), null, new Date(), null, null));
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());

			// ENVIO DE MAIL DE CONFIRMACIÓN DE ORDEN DE LIBRO CON MIMEMESSAGE
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Solicitud Libro | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 1rem;'>"
					+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
					+ ", hemos registrado vuestra orden de préstamo. </p><br/><br/>" + "<table border='0'>" + "<tr>"
					+ "<th>Código Préstamo</th>" + "<th>Libro</th>" + "<th>Local</th>" + "<th>Fecha Despacho</th>"
					+ "<th>Fecha Devolución</th>" + "</tr>" + "<tr>" + "<td>" + prestamo.getId() + "</td>" + "<td>"
					+ prestamo.getLibro().getTitulo() + " - " + prestamo.getLibro().getAutor() + "</td>" + "<td>"
					+ prestamo.getLibro().getLocal().getDireccion() + "</td>" + "<td>" + fechaDespacho + "</td>"
					+ "<td>" + fechaDevolucion + "</td>" + "</table><br/>"
					+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
					+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original, "
					+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
					+ "</body>"
					+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
					+ "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Solicitud Libro | Biblioteca2020", message);
			flash.addFlashAttribute("success", "Excelente! Su orden ha sido registrada!");
			flash.addFlashAttribute("confirma", false);
			// IR A PRESTAMOS PENDIENTES
			flash.addFlashAttribute("titulo", "Préstamos pendientes");
			flash.addFlashAttribute("prestamosPendientes", prestamoService
					.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(prestamo.getUsuario().getId()));
			// AQUI VALIDO SI LOS LIBROS SON MENORES QUE 10, SI LO SON, ENVÍO OTRO CORREO
			// PERO AL ADMIN
			// ESTA OPERACIÓN SE REALLIZARÁ MEDIANTE UN CRON JOB CON LA CLASE SCHEDULER
			/*
			 * Empleado adminLocal = empleadoService.findByRoleAndLocal("ADMIN",
			 * libroPrestamo.getLocal().getId()); if (libroPrestamo.getStock() < 10) {
			 * String messageAdmin = "<html><head>" + "<meta charset='UTF-8' />" +
			 * "<meta name='viewport' content='width=device-width, initial-scale=1.0' />" +
			 * "<title>Stock Libro | Biblioteca2020</title>" + "</head>" + "<body>" +
			 * "<div class='container' style='padding-top: 1rem;'>" +
			 * "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />" +
			 * "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " +
			 * adminLocal.getUsername() +
			 * ", le enviamos el siguiente correo para comunicarle que el stock del libro '"
			 * + prestamo.getLibro().getTitulo() + "', presente en el local situado en '" +
			 * prestamo.getLibro().getLocal().getDireccion() +
			 * "' es menor a las 10 unidades. </p><br/><br/>" +
			 * "<p>ESTE CORREO ES CONFIDENCIAL. Si usted no es el destinatario a quien se dirige el presente correo, "
			 * +
			 * "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
			 * +
			 * "incluyendo sus archivos, así como cualquier copia del mismo. O de lo contrario, podría incurrir en sanciones legales.</p>"
			 * + "</div>" + "</div>" + "</body>" +
			 * "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
			 * + "</html>";
			 * emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>",
			 * usuario.getEmail(), "Stock Libro | Biblioteca2020", messageAdmin); }
			 */
			return "redirect:/prestamos/prestamos-pendientes";
			// ERROR A LA HORA DE FORMATEAR FECHA
		} catch (ParseException pe) {
			model.addAttribute("error", pe.getMessage());
			return "/usuarios/biblioteca/solicitar-libro";
			// IR A BIBLIOTECA YA QUE HUBO UN ERROR A LA HORA DE ENVIAR EL CORREO
		} catch (MailException ex) {
			model.addAttribute("titulo", "Catálogo de libros");
			List<Libro> libros = libroService.findByTituloGroup();
			for (int i = 0; i < libros.size(); i++) {
				String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
				String descripcionFull = libros.get(i).getDescripcion().substring(150,
						libros.get(i).getDescripcion().length());
				libros.get(i).setDescripcionMin(descripcionMin + " ...");
				libros.get(i).setDescripcion(descripcionFull);
				model.addAttribute("libros", libros);
			}
			return "/usuarios/biblioteca";
			// ERROR GENERAL
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			// IR A BIBLIOTECA YA QUE HUBO UN ERROR A LA HORA DE GUARDAR LA ORDEN
			model.addAttribute("titulo", "Catálogo de libros");
			List<Libro> libros = libroService.findByTituloGroup();
			for (int i = 0; i < libros.size(); i++) {
				String descripcionMin = libros.get(i).getDescripcion().substring(0, 150);
				String descripcionFull = libros.get(i).getDescripcion().substring(150,
						libros.get(i).getDescripcion().length());
				libros.get(i).setDescripcionMin(descripcionMin + " ...");
				libros.get(i).setDescripcion(descripcionFull);
				model.addAttribute("libros", libros);
			}
			return "/usuarios/biblioteca";
		}
	}

	// FORMULARIO DE REGISTRO DE USUARIOS NUEVOS
	@GetMapping("/crear-perfil")
	public String perfil(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de usuario nuevo");
		return "/usuarios/perfil";
	}

	// REGRESAR A LA HOME
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cancelar-perfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	// REGISTRAR USUARIO NUEVO
	@PostMapping(value = "/crear-perfil")
	public String crearPerfil(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de usuario nuevo");
			return "/usuarios/perfil";
		}
		try {
			// VALIDACIÓN EMAIL
			Usuario usuarioExistente = usuarioService.findByEmailIgnoreCase(usuario.getEmail());
			if (usuarioExistente != null) {
				flash.addFlashAttribute("error", "El correo ya está asociado a otro usuario!");
				flash.addFlashAttribute("titulo", "Registro de usuario nuevo");
				return "redirect:/usuarios/perfil";
			} else {
				usuario.setFoto_usuario("no-image.jpg");
				usuario.setEstado(false);
				usuarioService.save(usuario);
				// AL CREAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
				Long idRole = usuario.getRoles().iterator().next().getId();
				usuarioLogService.save(new UsuarioLog(idRole, usuario.getNombres(), null, usuario.getApellidos(), null,
						usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
						usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
						usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null,
						"INSERT USER", new Date(), null, null));
			}
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de usuario nuevo");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/perfil";
		}
		// ENVIO DE MAIL CON MIMEMESSAGE
		try {
			// ESTÀ FUERA DE LA LÒGICA DE REGITRO PORQUE TENGO QUE VALIDAR QUE EL USUARIO
			// ESTÉ REGISTRADO ANTES DE MANDAR EL CORREO DE CONFIRMACIÒN
			ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
			confirmationTokenRepository.save(confirmationToken);
			// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
			// "http://localhost:8080"
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Completar Registro | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<img style='padding-top: 3rem;' src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<p>Saludos, hemos recibido tu peticiòn de registro a Biblioteca2020.</p><br/>"
					+ "<p style='padding-top: 1rem;'>Para confirmar tu cuenta, hacer click en el siguiente enlace: "
					+ "<a class='text-info' href='" + baseUrl + "/usuarios/cuenta-verificada?token="
					+ confirmationToken.getConfirmationToken() + "'>" + baseUrl + "/usuarios/cuenta-verificada?token="
					+ confirmationToken.getConfirmationToken() + "</a>" + "</p>" + "</div>" + "</div>" + "</body>"
					+ "<div class='footer' style='padding-top: 3rem;'>Biblioteca ©2020</div>" + "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Completar Registro | Biblioteca2020", message);
			model.addAttribute("titulo", "Registro exitoso");
			model.addAttribute("email", usuario.getEmail());
			return "/usuarios/registro-exitoso";
		} catch (MailException ex) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de usuario nuevo");
			model.addAttribute("error", ex.getMessage());
			return "/usuarios/perfil";
		}
	}

	// FORMULARIO DE VALIDACIÓN DE USUARIO ACTIVADO
	@RequestMapping(value = "/cuenta-verificada", method = { RequestMethod.GET, RequestMethod.POST })
	public String verificarCuenta(Model model, @RequestParam("token") String confirmationToken) {
		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		if (token != null) {
			try {
				Usuario usuario = usuarioService.findByEmailIgnoreCase(token.getUsuario().getEmail());
				Usuario usuarioOld = usuario;
				usuario.setEstado(true);
				usuarioService.update(usuario);
				// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
				Long idRole = usuario.getRoles().iterator().next().getId();
				usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
						usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
						usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(),
						usuarioOld.getEmail(), usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(),
						usuarioOld.getFecha_registro(), usuario.getFecha_registro(), usuarioOld.getUsername(),
						usuario.getUsername(), usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(),
						usuario.getEstado(), usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(),
						"VALIDATE ACCOUNT USER", null, new Date(), null));
				model.addAttribute("titulo", "Cuenta verificada");
				return "/usuarios/cuenta-verificada";
			} catch (Exception e) {
				model.addAttribute("error", "Error: " + e.getMessage());
				model.addAttribute("titulo", "Error");
				return "/usuarios/error-registro";
			}
		} else {
			model.addAttribute("error", "Lo sentimos, el enlace es invàlido o ya caducó!");
			model.addAttribute("titulo", "Error");
			return "/usuarios/error-registro";
		}
	}

	// FORMULARIO DE EDICIÓN DE PERFIL DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/editar-perfil")
	public String editarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		model.addAttribute("editable", true);
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Modificar datos del perfil");
		try {
			model.addAttribute("usuario", usuario);
			return "/usuarios/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	// ACTUALIZAR PERFIL DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/editar-perfil")
	public String guardarPerfil(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		try {
			if (result.hasErrors()) {
				model.addAttribute("usuario", usuario);
				model.addAttribute("editable", true);
				model.addAttribute("roles", roleService.findOnlyUsers());
				model.addAttribute("titulo", "Modificar datos del perfil");
				return "/usuarios/perfil";
			}
			// PREGUNTO SI EL PARAMETRO ES NULO ..
			if (!foto.isEmpty()) {
				// .. Y PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN CORRECTO
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					usuario.setFoto_usuario(foto.getOriginalFilename());
				} else {
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("usuario", usuario);
					model.addAttribute("editable", true);
					model.addAttribute("roles", roleService.findOnlyUsers());
					model.addAttribute("titulo", "Modificar datos del perfil");
					return "/usuarios/perfil";
				}
			}
		} catch (IOException e) {
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar datos del perfil");
			return "/usuarios/perfil";
		}
		try {
			Usuario usuarioOld = usuario;
			usuarioService.update(usuario);
			// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
					usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
					usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(), usuarioOld.getEmail(),
					usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(), usuarioOld.getFecha_registro(),
					usuario.getFecha_registro(), usuarioOld.getUsername(), usuario.getUsername(),
					usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(), usuario.getEstado(),
					usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(), "UPDATE BY USER", null, new Date(), null));
			flash.addFlashAttribute("warning", "La información de su perfil han sido actualizados en la base de datos");
			status.setComplete();
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar datos del perfil");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/perfil";
		}
	}

	// FORMULARIO DE CAMBIO DE CONTRASEÑA DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cambio-password")
	public String cambioPasswordUser(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(usuario.getId());
		model.addAttribute("cambiarPassword", cambiarPassword);
		model.addAttribute("titulo", "Cambio de contraseña");
		return "/usuarios/cambio-password";
	}

	// ACTUALIZACIÓN DE CONTRASEÑA DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping("/cambio-password")
	public String cambioPasswordUser(@Valid CambiarPassword cambiarPassword, BindingResult resultForm, Model model,
			RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		Usuario usuarioOld = usuario;
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getPasswordActual().equals("") || cambiarPassword.getNuevaPassword().equals("")
					|| cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios!");
				model.addAttribute("titulo", "Cambio de contraseña");
				return "/usuarios/cambio-password";
			}
			String result = resultForm.getAllErrors().stream().map(x -> x.getDefaultMessage())
					.collect(Collectors.joining(", "));
			model.addAttribute("cambiarPasswordError", result);
			model.addAttribute("titulo", "Cambio de contraseña");
			return "/usuarios/cambio-password";
		}
		try {
			usuarioService.cambiarPassword(cambiarPassword);
			// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
					usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
					usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(), usuarioOld.getEmail(),
					usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(), usuarioOld.getFecha_registro(),
					usuario.getFecha_registro(), usuarioOld.getUsername(), usuario.getUsername(),
					usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(), usuario.getEstado(),
					usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(), "CHANGE PASSWORD BY USER", null,
					new Date(), null));
			// ENVIO DE MAIL DE CONFIRMACIÓN CON MIMEMESSAGE
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Cambio de Contraseña | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 1rem;'>"
					+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
					+ ", recientemente ha actualizado su contraseña de usuario de Biblioteca2020.</p>"
					+ "<p>Recuerde no divulgar sus datos a terceros.</p>"
					+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
					+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
					+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
					+ "</body>"
					+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
					+ "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Cambio de Contraseña | Biblioteca2020", message);
			flash.addFlashAttribute("success", "Contraseña actualizada!");
			return "redirect:/home";
		} catch (MailException ex) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambio de contraseña");
			model.addAttribute("cambiarPasswordError", ex.getMessage());
			return "/usuarios/cambio-password";
		} catch (Exception e) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambio de contraseña");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/usuarios/cambio-password";
		}
	}

	// DESHABILITAR PERFIL DEL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/deshabilitar-perfil")
	public String deshabilitarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		Usuario usuarioOld = usuario;
		// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
		// "http://localhost:8080"
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		try {
			usuario.setEstado(false);
			usuarioService.update(usuario);
			// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
					usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
					usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(), usuarioOld.getEmail(),
					usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(), usuarioOld.getFecha_registro(),
					usuario.getFecha_registro(), usuarioOld.getUsername(), usuario.getUsername(),
					usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(), usuario.getEstado(),
					usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(), "LOCK ACCOUNT BY USER", null, new Date(),
					null));
			// ENVIO DE MAIL DE CONFIRMACIÓN CON MIMEMESSAGE
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Cuenta Deshabilitada | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 1rem;'>"
					+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
					+ ", acaba de deshabilitar su cuenta de usuario de Biblioteca2020.</p>"
					+ "<p>Para poder recuperar nuevamente su cuenta, ingrese a este enlace: "
					+ "<a class='text-info' href='" + baseUrl + "/usuarios/recuperar-cuenta'>" + baseUrl
					+ "/usuarios/recuperar-cuenta</a></p><br/>"
					+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
					+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
					+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
					+ "</body>"
					+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
					+ "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Cuenta Deshabilitada | Biblioteca2020", message);
			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL USUARIO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			return "redirect:/login";
		} catch (MailException ex) {
			flash.addFlashAttribute("error", ex.getMessage());
			return "redirect:/usuarios/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	// FORMULARIO DE OPCIONES DE CUENTA DE USUARIO
	@GetMapping(value = "/recuperar-cuenta")
	public String recuperarPerfilForm(RedirectAttributes flash, Authentication authentication, Model model) {
		model.addAttribute("titulo", "Recuperar cuenta");
		model.addAttribute("recuperarCuenta", new RecuperarCuenta());
		return "/usuarios/recuperar-cuenta";
	}

	// FORMULARIO DE REACTIVACIÓN DE CUENTA DE USUARIO
	@GetMapping(value = "/recuperar-cuenta/habilitar-cuenta")
	public String habilitarPerfilForm(RedirectAttributes flash, Authentication authentication, Model model) {
		model.addAttribute("titulo", "Solicitar reactivación de cuenta");
		model.addAttribute("recuperarCuenta", new RecuperarCuenta());
		return "/usuarios/habilitar-cuenta";
	}

	/*
	 * LÒGICA DE REACTIVACIÒN DE CUENTA: - EN LOGIN, DOY CLICK EN "RECUPERAR CUENTA"
	 * - EN EL FORMULARIO ESCRIBO LA EMAIL DE REGISTRO DE LA CUENTA Y EL DNI PARA
	 * VALIDAR EL USUARIO - SI VALIDA, MANDA UN CORREO DE SOLICITUD DE REACTIVACION
	 * DE MI CUENTA (PARECIDO AL REGISTRO POR 1RA VEZ) - VEO EL CORREO Y DOY CLIC EN
	 * EL ENLACE - ME REDIRECCIONA A LA PANTALLA DE CONFIRMACIÓN DEL ENLACE, QUE A
	 * SU VEZ, ME DIRECCIONA AL LOGIN
	 */
	@PostMapping(value = "/recuperar-cuenta/habilitar-cuenta")
	public String habilitarPerfil(@Valid RecuperarCuenta recuperarCuenta, BindingResult result,
			@RequestParam(name = "nroDocumento", required = false) String nroDocumento,
			@RequestParam(name = "email", required = false) String email, RedirectAttributes flash,
			Authentication authentication, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Recupera tu cuenta");
			model.addAttribute("recuperarCuenta", recuperarCuenta);
			return "/usuarios/habilitar-cuenta";
		}
		try {
			Usuario usuario = usuarioService.findByNroDocumentoAndEmail(recuperarCuenta.getNroDocumento(),
					recuperarCuenta.getEmail());
			if (usuario == null) {
				model.addAttribute("error", "Error, el DNI y/o correo ingresados son incorrectos!");
				model.addAttribute("titulo", "Solicitar reactivación de cuenta");
				return "/usuarios/habilitar-cuenta";
			} else if (usuario.getEstado().equals(true)) {
				model.addAttribute("info", "Esta cuenta ya està activada!");
				model.addAttribute("titulo", "Solicitar reactivación de cuenta");
				return "/usuarios/habilitar-cuenta";
			}
			// LÒGICA DE GENERACIÓN DE TOKEN DE CONFIRMACION CORREO
			ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
			confirmationTokenRepository.save(confirmationToken);
			// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
			// "http://localhost:8080"
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			// LÒGICA DE ENVIO CORREO, Y VALIDACIÓN CON MÈTODO EN COMÚN AL REGISTRO DE
			// USUARIOS NUEVOS
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Recuperar Cuenta | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<img style='padding-top: 3rem;' src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<p>Saludos, hemos recibido tu peticiòn de recuperación de tu cuenta.</p><br/>"
					+ "<p style='padding-top: 1rem;'>Para reactivar tu cuenta, entrar aquì: "
					+ "<a class='text-info' href='" + baseUrl + "/usuarios/cuenta-verificada?token="
					+ confirmationToken.getConfirmationToken() + "'>" + baseUrl + "/usuarios/cuenta-verificada?token="
					+ confirmationToken.getConfirmationToken() + "</a>" + "</p>" + "</div>" + "</div>" + "</body>"
					+ "<div class='footer' style='padding-top: 3rem;'>Biblioteca ©2020</div>" + "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Recuperar Cuenta | Biblioteca2020", message);
			model.addAttribute("titulo", "Solicitar reactivación de cuenta");
			model.addAttribute("email", usuario.getEmail());
			return "/usuarios/registro-exitoso";
		} catch (MailException ex) {
			flash.addFlashAttribute("error", ex.getMessage());
			return "redirect:/login";
		} catch (Exception e) {
			model.addAttribute("error",
					"Lo sentimos, su solicitud no puede ser enviada en estos momentos. Intèntelo mas tarde.");
			model.addAttribute("titulo", "Solicitar reactivación de cuenta");
			return "/usuarios/habilitar-cuenta";
		}
	}

	// FORMULARIO DE RECUPERACIÓN DE CONTRASEÑA DE USUARIO
	@GetMapping(value = "/recuperar-cuenta/recuperar-password")
	public String recuperarPasswordForm(RedirectAttributes flash, Authentication authentication, Model model) {
		model.addAttribute("titulo", "Recuperar contraseña");
		model.addAttribute("recuperarCuenta", new RecuperarCuenta());
		return "/usuarios/recuperar-password";
	}

	/*
	 * LÒGICA DE RECUPERACIÓN DE CONTRASEÑA: - EN LOGIN, DOY CLICK EN
	 * "RECUPERAR CUENTA" Y DESPUES EN "RECUPERACIÓN DE CUENTA VIA EMAIL" - EN EL
	 * FORMULARIO ESCRIBO LA EMAIL DE REGISTRO DE LA CUENTA Y EL DNI PARA VALIDAR EL
	 * USUARIO - SI VALIDA, MANDA UN CORREO DE SOLICITUD DE RECUPERACIÓN DE LA
	 * CONTRASEÑA (PARECIDO A LA REACTIVACION DE LA CUENTA) - VEO EL CORREO Y DOY
	 * CLIC EN EL ENLACE - ME REDIRECCIONA A LA PANTALLA DE CAMBIO DE CONTRASEÑA,
	 * QUE A SU VEZ, ME DIRECCIONA AL LOGIN
	 */
	@PostMapping(value = "/recuperar-cuenta/recuperar-password")
	public String cambioContraseña(@Valid RecuperarCuenta recuperarCuenta, BindingResult result,
			@RequestParam(name = "nroDocumento", required = false) String nroDocumento,
			@RequestParam(name = "email", required = false) String email, RedirectAttributes flash,
			Authentication authentication, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Recuperar contraseña");
			model.addAttribute("recuperarCuenta", recuperarCuenta);
			return "/usuarios/recuperar-password";
		}
		try {
			Usuario usuario = usuarioService.findByNroDocumentoAndEmail(recuperarCuenta.getNroDocumento(),
					recuperarCuenta.getEmail());
			if (usuario == null) {
				model.addAttribute("error", "Error, el DNI y/o correo ingresados son incorrectos!");
				model.addAttribute("titulo", "Recuperar contraseña");
				return "/usuarios/recuperar-password";
			} else if (usuario.getEstado().equals(false)) {
				model.addAttribute("error",
						"Lo sentimos, su cuenta està deshabilitada. Ir a 'Reactivación de cuenta'.");
				model.addAttribute("titulo", "Recuperar contraseña");
				return "/usuarios/recuperar-password";
			}
			// LÒGICA DE GENERACIÓN DE TOKEN DE CONFIRMACION CORREO
			ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
			confirmationTokenRepository.save(confirmationToken);
			// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
			// "http://localhost:8080"
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			// LÒGICA DE ENVIO CORREO, Y VALIDACIÓN CON MÈTODO EN COMÚN AL REGISTRO DE
			// USUARIOS NUEVOS
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Recuperar contraseña | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<img style='padding-top: 3rem;' src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 3rem;'>"
					+ "<p>Saludos, hemos recibido tu peticiòn de recuperar su contraseña.</p><br/>"
					+ "<p style='padding-top: 1rem;'>Para poder cambiar su contraseña, entrar aquì: "
					+ "<a class='text-info' href='" + baseUrl
					+ "/usuarios/recuperar-cuenta/recuperar-password/confirmar?token="
					+ confirmationToken.getConfirmationToken() + "'>" + baseUrl
					+ "/usuarios/recuperar-cuenta/recuperar-password/confirmar?token="
					+ confirmationToken.getConfirmationToken() + "</a>" + "</p>" + "</div>" + "</div>" + "</body>"
					+ "<div class='footer' style='padding-top: 3rem;'>Biblioteca ©2020</div>" + "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Recuperar contraseña | Biblioteca2020", message);
			model.addAttribute("titulo", "Recuperar contraseña");
			model.addAttribute("email", usuario.getEmail());
			return "/usuarios/registro-exitoso";
		} catch (MailException ex) {
			flash.addFlashAttribute("error", ex.getMessage());
			return "redirect:/login";
		} catch (Exception e) {
			model.addAttribute("error",
					"Lo sentimos, su solicitud no puede ser enviada en estos momentos. Intèntelo mas tarde.");
			model.addAttribute("titulo", "Recuperar contraseña");
			return "/usuarios/recuperar-password";
		}
	}

	// FORMULARIO DE VALIDACIÓN DE EMAIL DE USUARIO PARA LA RECUPERACIÓN DE
	// CONTRASEÑA
	@RequestMapping(value = "/recuperar-cuenta/recuperar-password/confirmar", method = RequestMethod.GET)
	public String verificarCuentaRecuperacionPassword(Model model, @RequestParam("token") String confirmationToken,
			Authentication authentication) {
		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		if (token != null) {
			try {
				CambiarPassword cambiarPassword = new CambiarPassword();
				Usuario usuario = usuarioService.findByUsername(token.getUsuario().getUsername());
				cambiarPassword.setId(usuario.getId());
				model.addAttribute("cambiarPassword", cambiarPassword);
				model.addAttribute("titulo", "Cambio de contraseña");
				return "/usuarios/actualizar-password";
			} catch (Exception e) {
				model.addAttribute("error", "Error: " + e.getMessage());
				model.addAttribute("titulo", "Error");
				return "/usuarios/error-registro";
			}
		} else {
			model.addAttribute("error", "Lo sentimos, el enlace es invàlido o ya caducó!");
			model.addAttribute("titulo", "Error");
			return "/usuarios/error-registro";
		}
	}

	// RECUPERACION DE CONTRASEÑA DEL USUARIO
	@PostMapping("/recuperar-cuenta/recuperar-password/confirmar")
	public String recuperarPasswordUser(@Valid CambiarPassword cambiarPassword, BindingResult resultForm, Model model,
			RedirectAttributes flash, Authentication authentication) {
		// SETEO ESTE CAMPO ASI, YA QUE NO LO NECESITO EN MI DTO
		cambiarPassword.setPasswordActual("");
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getNuevaPassword().equals("") || cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios!");
				model.addAttribute("titulo", "Cambio de contraseña");
				return "/usuarios/actualizar-password";
			}
		}
		try {
			Usuario usuario = usuarioService.findById(cambiarPassword.getId());
			Usuario usuarioOld = usuario;
			usuarioService.recuperarPassword(cambiarPassword);
			// AQUI, TENDRIA QUE BORRAR EL TOKEN O ANULARLO, PARA EVITAR REUTILIRAR EL MISMO
			// TOKEN PARA REALIZAR LA MISMA OPERACION, POR ERROR
			// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
					usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
					usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(), usuarioOld.getEmail(),
					usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(), usuarioOld.getFecha_registro(),
					usuario.getFecha_registro(), usuarioOld.getUsername(), usuario.getUsername(),
					usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(), usuario.getEstado(),
					usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(), "RECOVERY PASSWORD BY USER", null,
					new Date(), null));
			// ENVIO DE MAIL DE CONFIRMACIÓN CON MIMEMESSAGE
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Cambio de Contraseña | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 1rem;'>"
					+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
					+ ", recientemente ha actualizado su contraseña de usuario de Biblioteca2020.</p>"
					+ "<p>Recuerde no divulgar sus datos a terceros.</p>"
					+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
					+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
					+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
					+ "</body>"
					+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
					+ "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Cambio de Contraseña | Biblioteca2020", message);
			flash.addFlashAttribute("success", "Contraseña actualizada! Iniciar sesión con sus nuevas credenciales");
			return "redirect:/login";
		} catch (MailException ex) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambio de contraseña");
			model.addAttribute("cambiarPasswordError", ex.getMessage());
			return "/usuarios/actualizar-password";
		} catch (Exception e) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambio de contraseña");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/usuarios/actualizar-password";
		}
	}

	// ########################## CRUD ##########################
	// LISTADO DE USUARIOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/listar")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("usuarios", usuarioService.findAll());
		model.addAttribute("titulo", "Listado de usuarios");
		return "/usuarios/listar";
	}

	// FORMULARIO DE REGISTRO DE USUARIO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/crear")
	public String crearFormUsuario(Model model) {
		Usuario usuario = new Usuario();
		usuario.setFoto_usuario("no-image.jpg");
		model.addAttribute("usuario", usuario);
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de usuario nuevo");
		return "usuarios/crear";
	}

	// REGISTRO DE USUARIO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		try {
			if (result.hasErrors()) {
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findOnlyUsers());
				model.addAttribute("titulo", "Registro de usuario nuevo");
				return "/usuarios/crear";
			}
			// PREGUNTO SI EL PARAMETRO ES NULO
			if (!foto.isEmpty()) {
				// PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					usuario.setFoto_usuario(foto.getOriginalFilename());
				} else {
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("usuario", usuario);
					model.addAttribute("roles", roleService.findOnlyUsers());
					model.addAttribute("titulo", "Registro de usuario nuevo");
					return "/usuarios/crear";
				}
			} else {
				usuario.setFoto_usuario("no-image.jpg");
			}
		} catch (IOException e) {
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de usuario nuevo");
			return "/usuarios/crear";
		}
		try {
			usuario.setEstado(true);
			usuarioService.save(usuario);
			// AL CREAR EL USUARIO, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			String tipoOp = "";
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "INSERT BY SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "INSERT BY ADMIN";
					break;
			}
			usuarioLogService.save(new UsuarioLog(idRole, usuario.getNombres(), null, usuario.getApellidos(), null,
					usuario.getNroDocumento(), null, usuario.getDireccion(), null, usuario.getEmail(), null,
					usuario.getCelular(), null, usuario.getFecha_registro(), null, usuario.getUsername(), null,
					usuario.getPassword(), null, usuario.getEstado(), null, usuario.getFoto_usuario(), null, tipoOp,
					new Date(), null, null));
			flash.addFlashAttribute("success",
					"El usuario ha sido registrado en la base de datos (Código " + usuario.getId() + ")");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de usuario nuevo");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	// DESHABILITAR USUARIO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String tipoOp = "";
		String ruta = "";
		try {
			Usuario usuario = usuarioService.findById(id);
			Usuario usuarioOld = usuario;
			usuario.setEstado(false);
			usuarioService.update(usuario);
			// AL ACTUALIZAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = usuario.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "LOCK ACCOUNT BY SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "LOCK ACCOUNT BY ADMIN";
					break;
			}
			usuarioLogService.save(new UsuarioLog(idRole, usuarioOld.getNombres(), usuario.getNombres(),
					usuarioOld.getApellidos(), usuario.getApellidos(), usuarioOld.getNroDocumento(),
					usuario.getNroDocumento(), usuarioOld.getDireccion(), usuario.getDireccion(), usuarioOld.getEmail(),
					usuario.getEmail(), usuarioOld.getCelular(), usuario.getCelular(), usuarioOld.getFecha_registro(),
					usuario.getFecha_registro(), usuarioOld.getUsername(), usuario.getUsername(),
					usuarioOld.getPassword(), usuario.getPassword(), usuarioOld.getEstado(), usuario.getEstado(),
					usuarioOld.getFoto_usuario(), usuario.getFoto_usuario(), tipoOp, null, new Date(), null));
			flash.addFlashAttribute("info", "El usuario con código " + usuario.getId() + " ha sido deshabilitado.");
			ruta = "redirect:/usuarios/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			ruta = "redirect:/usuarios/listar";
		}
		return ruta;
	}

	// ############################# REPORTES #############################
	// FORMULARIO DE REPORTES DE USUARIO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/reportes")
	public String crearReporte(Model model, Authentication authentication) {
		model.addAttribute("titulo", "Creación de Reportes");
		model.addAttribute("usuario", new Usuario());
		ArrayList<Boolean> estados = new ArrayList<Boolean>();
		estados.add(true);
		estados.add(false);
		model.addAttribute("estados", estados);
		return "/usuarios/crear_reporte";
	}

	// GENERAR REPORTE PDF USUARIOS TOTALES
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/reportes/pdf/usuarios-totales", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfUsuariosTotal() {
		List<Usuario> usuarios = null;
		usuarios = usuarioService.findAll();
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (usuarios.size() != 0) {
				bis = GenerarReportePDF.generarPDFUsuarios("Reporte de usuarios totales", usuarios);
				headers.add("Content-Disposition", "inline; filename=listado-usuarios-totales.pdf");
				return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
						.body(new InputStreamResource(bis));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
		} catch (Exception e) {
			headers.clear();
			headers.add("Location", "/error_reporte");
			return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
		}
	}

	// GENERAR REPORTE PDF USUARIOS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/reportes/pdf/usuarios-por-estado/{estado}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfUsuariosPorEstado(@PathVariable("estado") String estado,
			Model model) {
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			List<Usuario> usuarios = usuarioService.findAll();
			String titulo = "";
			String tituloPdf = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				// FILTRO SOLAMENTE LOS USUARIOS ACTIVOS
				for (int i = 0; i < usuarios.size(); i++) {
					usuarios.removeIf(n -> n.getEstado().equals(false));
				}
				titulo = "listado-usuarios-disponibles";
				tituloPdf = "Reporte de usuarios disponibles";
			} else if (estado.equals("false")) {
				// FILTRO SOLAMENTE LOS USUARIOS INACTIVOS
				for (int i = 0; i < usuarios.size(); i++) {
					usuarios.removeIf(n -> n.getEstado().equals(true));
				}
				titulo = "listado-usuarios-no-disponibles";
				tituloPdf = "Reporte de usuarios no disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (usuarios.size() != 0) {
				bis = GenerarReportePDF.generarPDFUsuarios(tituloPdf, usuarios);
				headers.add("Content-Disposition", "inline; filename=" + titulo + ".pdf");
				return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
						.body(new InputStreamResource(bis));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				headers.set("errorMessage", "Error, el reporte solicitado no existe");
				model.addAttribute("errorMessage", "Error, el reporte solicitado no existe");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
		} catch (IllegalArgumentException ex) {
			headers.clear();
			headers.add("Location", "/error_reporte");
			return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
		} catch (Exception e) {
			headers.clear();
			headers.add("Location", "/error_reporte");
			return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
		}
	}

	// GENERAR REPORTE EXCEL USUARIOS TOTALES
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/reportes/xlsx/usuarios-totales", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> generarExcelUsuariosTotal() {
		List<Usuario> usuarios = usuarioService.findAll();
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (usuarios.size() != 0) {
				bis = GenerarReporteExcel.generarExcelUsuarios("Reporte de usuarios totales", usuarios);
				headers.add("Content-Disposition", "attachment; filename=listado-usuarios-totales.xlsx");
				return ResponseEntity.ok().headers(headers).body(new InputStreamResource(bis));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
		} catch (Exception e) {
			headers.clear();
			headers.add("Location", "/error_reporte");
			return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
		}
	}

	// GENERAR REPORTE EXCEL USUARIOS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/reportes/xlsx/usuarios-por-estado/{estado}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repUsuariosPorEstado(@PathVariable("estado") String estado) {
		ByteArrayInputStream in;
		List<Usuario> usuarios = usuarioService.findAll();
		var headers = new HttpHeaders();
		try {
			String titulo = "";
			String tituloExcel = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				for (int i = 0; i < usuarios.size(); i++) {
					usuarios.removeIf(n -> n.getEstado().equals(false));
				}
				titulo = "listado-usuarios-disponibles";
				tituloExcel = "Reporte de usuarios disponibles";
			} else if (estado.equals("false")) {
				for (int i = 0; i < usuarios.size(); i++) {
					usuarios.removeIf(n -> n.getEstado().equals(true));
				}
				titulo = "listado-usuarios-no-disponibles";
				tituloExcel = "Reporte de usuarios no disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (usuarios.size() != 0) {
				in = GenerarReporteExcel.generarExcelUsuarios(tituloExcel, usuarios);
				headers.add("Content-Disposition", "attachment; filename=" + titulo + ".xlsx");
				return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
		} catch (Exception e) {
			headers.clear();
			headers.add("Location", "/error_reporte");
			return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
		}
	}

	// REGRESAR A LISTADO DE USUARIOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/usuarios/listar";
	}
}
