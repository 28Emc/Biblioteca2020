package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
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
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.EmailSenderService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.IPrestamoService;
import com.biblioteca2020.models.service.IRoleService;
import com.biblioteca2020.models.service.IUsuarioService;

@Controller
@RequestMapping("/usuarios")
@SessionAttributes("usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IPrestamoService prestamoService;

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

	// ############################ ROLE USER ############################
	// CATÁLOGO DE LIBROS PARA EL USUARIO
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

	// DISPONIBILIDAD DE LIBRO SEGUN SU TITULO
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

	// CARGAR FORMULARIO DE ORDEN DE PRÉSTAMO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca/solicitar-libro/{id}/{titulo}")
	public String solicitarLibroForm(@PathVariable("titulo") String titulo, @PathVariable("id") Long id_local,
			Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		Libro libro = libroService.findByTituloAndLocalAndEstado(titulo, id_local, true);
		model.addAttribute("titulo", "Solicitar Libro");
		model.addAttribute("libro", libro);
		model.addAttribute("titulo_libro", titulo);
		model.addAttribute("id_local", id_local);
		model.addAttribute("usuario", usuario);
		model.addAttribute("prestamo", new Prestamo());
		return "/usuarios/biblioteca/solicitar-libro";
	}

	// GENERAR ORDEN DE PRÉSTAMO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@PostMapping(value = "/biblioteca/solicitar-libro")
	public String solicitarLibro(Prestamo prestamo, @RequestParam(name = "id_libro", required = false) Long id_libro,
			@RequestParam(name = "id_usuario", required = false) Long id_usuario,
			@RequestParam(name = "fecha_devolucion", required = false) String fecha_devolucion,
			RedirectAttributes flash, SessionStatus status, Model model, Authentication authentication) {
		try {
			if (id_libro == null || id_usuario == null || fecha_devolucion == null) {
				model.addAttribute("prestamo", prestamo);
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

			/* LÓGICA DE REGISTRO DE ORDEN DE LIBRO */
			// LIBRO
			Libro libroAPrestar = libroService.findByTituloAndLocalAndEstado(libroService.findOne(id_libro).getTitulo(),
					libroService.findOne(id_libro).getLocal().getId(), true);
			prestamo.setLibro(libroAPrestar);
			// ACTUALIZAR STOCK LIBRO
			if (libroAPrestar.getStock() <= 0) {
				model.addAttribute("error", "Lo sentimos, no hay stock suficiente del libro seleccionado ("
						+ libroAPrestar.getTitulo() + ")");
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
				libroAPrestar.setStock(libroAPrestar.getStock() - 1);
			}
			// USUARIO
			prestamo.setUsuario(usuarioService.findById(id_usuario));
			// EMPLEADO
			// EL EMPLEADO SE SETEA CON EL USUARIO DE PRUEBA DEL LOCAL DEL LIBRO POR PARTE
			// DEL USUARIO
			prestamo.setEmpleado(empleadoService.findByUsernameAndLocal("Prueba", libroAPrestar.getLocal().getId()));
			// FECHA_DESPACHO
			Date fechaDespacho = new Date();
			prestamo.setFecha_despacho(fechaDespacho);
			// FECHA DEVOLUCIÓN
			// try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaDevolucionPrestamo = null;
			fechaDevolucionPrestamo = formatter.parse(fecha_devolucion);
			prestamo.setFecha_devolucion(fechaDevolucionPrestamo);

			// USO CALENDAR PARA MOSTRAR LA FECHA DE DEVOLUCION
			Calendar calendar = Calendar.getInstance(new Locale("es", "ES"));
			calendar.setTime(prestamo.getFecha_despacho());
			// MOSTRAR FECHA POR DIA, MES Y ANIO
			String anio = String.valueOf(calendar.get(Calendar.YEAR));
			String mes = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es", "ES"));
			String dia = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			String fechaPrestamoHoy = dia + " de " + mes + " " + anio;
			// OBSERVACIONES
			prestamo.setObservaciones("El usuario: " + prestamo.getUsuario().getNombres() + ", "
					+ prestamo.getUsuario().getApellidos() + "(DNI " + prestamo.getUsuario().getNroDocumento()
					+ ") ha solicitado el libro: " + prestamo.getLibro().getTitulo() + " el dìa " + fechaPrestamoHoy
					+ ", hasta el dìa " + prestamo.getFecha_devolucion() + ". A la espera de confirmación.");
			// DEVOLUCION
			prestamo.setDevolucion(false);
			prestamoService.save(prestamo);

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());

			// ENVIO DE MAIL DE CONFIRMACIÓN DE ORDEN DE LIBRO CON MIMEMESSAGE
			String message = "<html><head>" + "<meta charset='UTF-8' />"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
					+ "<title>Solicitud Libro | Biblioteca2020</title>" + "</head>" + "<body>"
					+ "<div class='container' style='padding-top: 1rem;'>"
					+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
					+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos " + usuario.getUsername()
					+ ", hemos recibido su orden de préstamo. </p><br/><br/>" + "<table border='0'>" + "<tr>"
					+ "<th>Código Préstamo</th>" + "<th>Libro</th>" + "<th>Local</th>" + "<th>Fecha Despacho</th>"
					+ "<th>Fecha Devolución</th>" + "</tr>" + "<tr>" + "<td>" + prestamo.getId() + "</td>" + "<td>"
					+ prestamo.getLibro().getTitulo() + " - " + prestamo.getLibro().getAutor() + "</td>" + "<td>"
					+ prestamo.getLibro().getLocal().getDireccion() + "</td>"
					// FALTA FORMATEAR LAS FECHAS EN EL FORMATO yyyy/mm/dd
					+ "<td>" + prestamo.getFecha_despacho() + "</td>" + "<td>" + prestamo.getFecha_devolucion()
					+ "</td>" + "</table><br/>"
					+ "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
					+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
					+ "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
					+ "</body>"
					+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
					+ "</html>";
			emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
					"Solicitud Libro | Biblioteca2020", message);

			flash.addFlashAttribute("success", "Excelente! Su orden ha sido registrada!");
			flash.addFlashAttribute("confirma", false);
			// IR A PRESTAMOS PENDIENTES
			flash.addFlashAttribute("titulo", "Préstamos Pendientes");
			flash.addFlashAttribute("prestamosPendientes", prestamoService
					.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(prestamo.getUsuario().getId()));

			// AQUI VALIDO SI LOS LIBROS SON MENORES QUE 10, SI LO SON, ENVÍO OTRO CORREO
			// PERO AL ADMIN
			Empleado adminLocal = empleadoService.findByRoleAndLocal("ADMIN", libroAPrestar.getLocal().getId());
			if (libroAPrestar.getStock() < 10) {
				String messageAdmin = "<html><head>" + "<meta charset='UTF-8' />"
						+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
						+ "<title>Stock Libro | Biblioteca2020</title>" + "</head>" + "<body>"
						+ "<div class='container' style='padding-top: 1rem;'>"
						+ "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
						+ "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos "
						+ adminLocal.getUsername()
						+ ", le enviamos el siguiente correo para comunicarle que el stock del libro '"
						+ prestamo.getLibro().getTitulo() + "', presente en el local situado en '"
						+ prestamo.getLibro().getLocal().getDireccion() + "' es menor a las 10 unidades. </p><br/><br/>"
						+ "<p>ESTE CORREO ES CONFIDENCIAL. Si usted no es el destinatario a quien se dirige el presente correo, "
						+ "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
						+ "incluyendo sus archivos, así como cualquier copia del mismo. O de lo contrario, podría incurrir en sanciones legales.</p>"
						+ "</div>" + "</div>" + "</body>"
						+ "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
						+ "</html>";
				emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
						"Stock Libro | Biblioteca2020", messageAdmin);
			}

			return "redirect:/prestamos/prestamos-pendientes";

			// ERROR A LA HJORA DE FORMATEAR FECHA
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

		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
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

	@GetMapping("/crear-perfil")
	public String perfil(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de Usuario");
		return "/usuarios/perfil";
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cancelar-perfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	@PostMapping(value = "/crear-perfil")
	public String crearPerfil(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/perfil";
		}
		/* VALIDACIÓN EMAIL */
		Usuario usuarioExistente = usuarioService.findByEmailIgnoreCase(usuario.getEmail());
		if (usuarioExistente != null) {
			flash.addFlashAttribute("error", "El correo ya está asociado a otro usuario!");
			flash.addFlashAttribute("titulo", "Registro de Usuario");
			return "redirect:/usuarios/perfil";
		} else {
			usuario.setFoto_usuario("no-image.jpg");
			try {
				usuarioService.save(usuario);
			} catch (Exception e) {
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findOnlyUsers());
				model.addAttribute("titulo", "Registro de Usuario");
				model.addAttribute("error", e.getMessage());
				return "/usuarios/perfil";
			}
			// ESTÀ FUERA DE LA LÒGICA DE REGITRO PORQUE TENGO QUE VALIDAR QUE EL USUARIO
			// ESTÉ REGISTRADO ANTES DE MANDAR EL CORREO DE CONFIRMACIÒN
			ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
			confirmationTokenRepository.save(confirmationToken);
			// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
			// "http://localhost:8080"
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			// ENVIO DE MAIL CON MIMEMESSAGE
			try {
				String message = "<html><head>" + "<meta charset='UTF-8' />"
						+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
						+ "<title>Completar Registro | Biblioteca2020</title>" + "</head>" + "<body>"
						+ "<div class='container' style='padding-top: 3rem;'>"
						+ "<img style='padding-top: 3rem;' src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
						+ "<div class='container' style='padding-top: 3rem;'>"
						+ "<p>Saludos, hemos recibido tu peticiòn de registro a Biblioteca2020.</p><br/>"
						+ "<p style='padding-top: 1rem;'>Para confirmar tu cuenta, entrar aquì: "
						+ "<a class='text-info' href='" + baseUrl + "/usuarios/cuenta-verificada?token="
						+ confirmationToken.getConfirmationToken() + "'>" + baseUrl
						+ "/usuarios/cuenta-verificada?token=" + confirmationToken.getConfirmationToken() + "</a>"
						+ "</p>" + "</div>" + "</div>" + "</body>"
						+ "<div class='footer' style='padding-top: 3rem;'>Biblioteca ©2020</div>" + "</html>";
				emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
						"Completar Registro | Biblioteca2020", message);
				model.addAttribute("titulo", "Registro exitoso");
				model.addAttribute("email", usuario.getEmail());
				return "/usuarios/registro-exitoso";
			} catch (MailException ex) {
				model.addAttribute("usuario", usuario);
				model.addAttribute("roles", roleService.findOnlyUsers());
				model.addAttribute("titulo", "Registro de Usuario");
				model.addAttribute("error", ex.getMessage());
				return "/usuarios/perfil";
			}
		}
	}

	@RequestMapping(value = "/cuenta-verificada", method = { RequestMethod.GET, RequestMethod.POST })
	public String verificarCuenta(Model model, @RequestParam("token") String confirmationToken) {
		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		if (token != null) {
			try {
				Usuario usuario = usuarioService.findByEmailIgnoreCase(token.getUsuario().getEmail());
				usuario.setEstado(true);
				usuarioService.update(usuario);
				model.addAttribute("titulo", "Cuenta Verificada");
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

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/editar-perfil")
	public String editarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		model.addAttribute("editable", true);
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Modificar Usuario");
		try {
			model.addAttribute("usuario", usuario);
			return "/usuarios/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/editar-perfil")
	public String guardarPerfil(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			return "/usuarios/perfil";
		}
		if (!foto.isEmpty()) {
			Path rootPath = Paths.get("uploads").resolve(foto.getOriginalFilename());
			Path rootAbsolutePath = rootPath.toAbsolutePath();
			try {
				Files.copy(foto.getInputStream(), rootAbsolutePath);
				usuario.setFoto_usuario(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		} else if (usuario.getFoto_usuario() == null || usuario.getFoto_usuario() == "") {
			usuario.setFoto_usuario("no-image.jpg");
		}
		try {
			usuarioService.update(usuario);
			flash.addFlashAttribute("warning",
					"La información de su perfil han sido actualizados en la base de datos.");
			status.setComplete();
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cambio-password")
	public String cambioPasswordUser(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(usuario.getId());
		model.addAttribute("cambiarPassword", cambiarPassword);
		model.addAttribute("titulo", "Cambiar Password");
		return "/usuarios/cambio-password";
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping("/cambio-password")
	public String cambioPasswordUser(@Valid CambiarPassword cambiarPassword, BindingResult resultForm, Model model,
			RedirectAttributes flash, Authentication authentication) {
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getPasswordActual().equals("") || cambiarPassword.getNuevaPassword().equals("")
					|| cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios");
				model.addAttribute("titulo", "Cambiar Password");
				return "/usuarios/cambio-password";
			}
			String result = resultForm.getAllErrors().stream().map(x -> x.getDefaultMessage())
					.collect(Collectors.joining(", "));
			model.addAttribute("cambiarPasswordError", result);
			model.addAttribute("titulo", "Cambiar Password");
			return "/usuarios/cambio-password";
		}
		try {
			usuarioService.cambiarPassword(cambiarPassword);
		} catch (Exception e) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/usuarios/cambio-password";
		}

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());

		// ENVIO DE MAIL DE CONFIRMACIÓN CON MIMEMESSAGE
		try {
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

			flash.addFlashAttribute("success", "Password Actualizada");
			return "redirect:/home";
		} catch (MailException ex) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPasswordError", ex.getMessage());
			return "/usuarios/cambio-password";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/deshabilitar-perfil")
	public String deshabilitarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
		// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
		// "http://localhost:8080"
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		try {
			usuario.setEstado(false);
			usuarioService.update(usuario);
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

			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
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

	@GetMapping(value = "/recuperar-cuenta")
	public String habilitarPerfilForm(RedirectAttributes flash, Authentication authentication, Model model) {
		model.addAttribute("titulo", "Recupera tu cuenta");
		model.addAttribute("recuperarCuenta", new RecuperarCuenta());
		return "/usuarios/recuperar-cuenta";
	}

	/*
	 * LÒGICA DE REACTIVACIÒN DE CUENTA: - EN LOGIN, DOY CLICK EN "RECUPERAR CUENTA"
	 * - EN EL FORMULARIO ESCRIBO LA EMAIL DE REGISTRO DE LA CUENTA Y EL DNI PARA
	 * VALIDAR EL USUARIO - SI VALIDA, MANDA UN CORREO DE SOLICITUD DE REACTIVACION
	 * DE MI CUENTA (PARECIDO AL REGISTRO POR 1RA VEZ) - VEO EL CORREO Y DOY CLIC EN
	 * EL ENLACE - ME REDIRECCIONA A LA PANTALLA DE CONFIRMACIÓN DEL ENLACE, QUE A
	 * SU VEZ, ME DIRECCIONA AL LOGIN
	 */
	@PostMapping(value = "/recuperar-cuenta")
	public String habilitarPerfil(@Valid RecuperarCuenta recuperarCuenta, BindingResult result,
			@RequestParam(name = "nroDocumento", required = false) String nroDocumento,
			@RequestParam(name = "email", required = false) String email, RedirectAttributes flash,
			Authentication authentication, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Recupera tu cuenta");
			model.addAttribute("recuperarCuenta", recuperarCuenta);
			return "/usuarios/recuperar-cuenta";
		}
		try {
			Usuario usuario = usuarioService.findByNroDocumentoAndEmailAndEstado(recuperarCuenta.getNroDocumento(),
					recuperarCuenta.getEmail(), false);
			// LÒGICA DE GENERACIÓN DE TOKEN DE CONFIRMACION CORREO
			ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
			confirmationTokenRepository.save(confirmationToken);
			// OBTENER PATH DEL SERVIDOR EN LA PETICION ACTUAL, ES DECIR
			// "http://localhost:8080"
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			// LÒGICA DE ENVIO CORREO, Y VALIDACIÓN CON MÈTODO EN COMÚN AL REGISTRO DE
			// USUARIOS NUEVOS
			try {
				String message = "<html><head>" + "<meta charset='UTF-8' />"
						+ "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
						+ "<title>Recuperar Cuenta | Biblioteca2020</title>" + "</head>" + "<body>"
						+ "<div class='container' style='padding-top: 3rem;'>"
						+ "<img style='padding-top: 3rem;' src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
						+ "<div class='container' style='padding-top: 3rem;'>"
						+ "<p>Saludos, hemos recibido tu peticiòn de recuperación de tu cuenta.</p><br/>"
						+ "<p style='padding-top: 1rem;'>Para reactivar tu cuenta, entrar aquì: "
						+ "<a class='text-info' href='" + baseUrl + "/usuarios/cuenta-verificada?token="
						+ confirmationToken.getConfirmationToken() + "'>" + baseUrl
						+ "/usuarios/cuenta-verificada?token=" + confirmationToken.getConfirmationToken() + "</a>"
						+ "</p>" + "</div>" + "</div>" + "</body>"
						+ "<div class='footer' style='padding-top: 3rem;'>Biblioteca ©2020</div>" + "</html>";
				emailSenderService.sendMail("Biblioteca2020 <edmech25@gmail.com>", usuario.getEmail(),
						"Recuperar Cuenta | Biblioteca2020", message);
				model.addAttribute("titulo", "Recupera tu Cuenta");
				model.addAttribute("email", usuario.getEmail());
				return "/usuarios/registro-exitoso";
			} catch (MailException ex) {
				flash.addFlashAttribute("error", ex.getMessage());
				return "redirect:/login";
			}
		} catch (Exception e) {
			model.addAttribute("error",
					"Lo sentimos, el DNI y/o correo ingresados son incorrectos, o tu cuenta no necesita reactivarse.");
			model.addAttribute("titulo", "Recupera tu cuenta");
			return "/usuarios/recuperar-cuenta";
		}
	}

	// ############################ ROLE ADMIN, EMPLEADO
	// ############################
	// LISTADO DE USUARIOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/listar")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("usuarios", usuarioService.findAll());
		model.addAttribute("titulo", "Listado de Usuarios");
		return "usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/usuarios/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/crear")
	public String crearFormUsuario(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/crear";
		}
		if (!foto.isEmpty()) {
			String rootPath = "C://Temp//uploads";
			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				usuario.setFoto_usuario(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}
		try {
			usuarioService.save(usuario);
			flash.addFlashAttribute("success",
					"El usuario ha sido registrado en la base de datos (Código " + usuario.getId() + ")");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		try {
			Usuario usuario = usuarioService.findById(id);
			usuario.setEstado(false);
			usuarioService.update(usuario);
			flash.addFlashAttribute("info", "El usuario con código " + usuario.getId() + " ha sido deshabilitado.");
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}
}
