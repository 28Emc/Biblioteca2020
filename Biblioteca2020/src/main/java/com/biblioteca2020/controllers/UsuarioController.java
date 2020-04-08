package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
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
import com.biblioteca2020.models.dao.IConfirmationTokenDao;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.ConfirmationToken;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.models.service.EmailSenderService;
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

	// ############################ ROLE USER ############################
	// CATÁLOGO DE LIBROS PARA EL USUARIO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca")
	public String listarAllLibrosUser(Model model, Principal principal) {
		model.addAttribute("titulo", "Catálogo de libros");
		// ESTE MÈTODO ES DE REFERENCIA NADA MAS, A LA HORA DE VER LA DISPONIBILIDAD,
		// AHI SI FILTRO POR LOCAL
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
		model.addAttribute("libroDetalle", libros.get(0));
		return "/usuarios/biblioteca/ver";
	}

	// CARGAR FORMULARIO DE ORDEN DE PRÉSTAMO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@GetMapping("/biblioteca/solicitarLibro/{id}/{titulo}")
	public String solicitarLibroForm(@PathVariable("titulo") String titulo, @PathVariable("id") Long id_local,
			Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		Libro libro = libroService.findByTituloAndLocalAndEstado(titulo, id_local, true);
		model.addAttribute("titulo", "Solicitar Libro");
		model.addAttribute("libro", libro);
		// model.addAttribute("local", libro.getLocal());
		model.addAttribute("usuario", usuario);
		model.addAttribute("prestamo", new Prestamo());
		return "/usuarios/biblioteca/solicitarLibro";
	}

	// GENERAR ORDEN DE PRÉSTAMO
	// TE QUEDASTE AQUI - NO FUNCIONE ESTE MÈTODO - 2.04.2020
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_USER')")
	@PostMapping("/biblioteca/solicitarLibro")
	public String solicitarLibro(@Valid Prestamo prestamo,
			@RequestParam(name = "id_libro", required = false) Long id_libro,
			@RequestParam(name = "id_usuario", required = false) Long id_usuario,
			@RequestParam(name = "fechaDevolucion", required = false) String fechaDevolucion, RedirectAttributes flash,
			SessionStatus status, Model model, Authentication authentication) {
		// System.out.println(libro.getId() + ", " + usuario.getId() + ", " +
		// fechaDevolucion);
		try {
			if (id_libro == null || id_usuario == null || fechaDevolucion == null) {
				// model.addAttribute("libro", libroService.findOne(id_libro));
				// model.addAttribute("local", libroService.findOne(id_libro).getLocal());
				// model.addAttribute("usuario", usuarioService.findById(id_usuario));
				model.addAttribute("prestamo", prestamo);
				model.addAttribute("error",
						"El prestamo necesita un libro, un usuario y una fecha de despacho VÁLIDOS.");
				return "/usuarios/biblioteca/solicitarLibro";
			}

			/* LÓGICA DE REGISTRO DE ORDEN DE LIBRO */
			// LA ORDEN DE PRÈSTAMO VALIDA ID_LIBRO, ID_USUARIO,
			// FECHA_DESPACHO, FECHA_DEVOLUCIÒN Y OBSERVACIONES.
			// LIBRO
			Libro libroAPrestar = libroService.findByTituloAndLocalAndEstado(libroService.findOne(id_libro).getTitulo(),
					libroService.findOne(id_libro).getLocal().getId(), true);
			prestamo.setLibro(libroAPrestar);
			// ACTUALIZAR STOCK LIBRO
			if (libroAPrestar.getStock() <= 0) {
				model.addAttribute("error", "Lo sentimos, no hay stock suficiente del libro seleccionado ("
						+ libroAPrestar.getTitulo() + ")");
				return "/usuarios/biblioteca/solicitarLibro";
			} else {
				libroAPrestar.setStock(libroAPrestar.getStock() - 1);
			}
			// USUARIO
			prestamo.setUsuario(usuarioService.findById(id_usuario));
			// EMPLEADO
			prestamo.setEmpleado(null);
			// FECHA_DESPACHO
			Date fechaDespacho = new Date();
			prestamo.setFecha_despacho(fechaDespacho);
			// FECHA DEVOLUCIÓN
			try {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date fechaDevolucionPrestamo = null;
				fechaDevolucionPrestamo = formatter.parse(fechaDevolucion);
				prestamo.setFecha_devolucion(fechaDevolucionPrestamo);
			} catch (ParseException pe) {
				model.addAttribute("error", pe.getMessage());
				return "/usuarios/biblioteca/solicitarLibro";
			}
			// USO CALENDAR PARA MOSTRAR LA FECHA DE DEVOLUCION
			Calendar calendar = Calendar.getInstance(new Locale("es", "ES"));
			calendar.setTime(prestamo.getFecha_devolucion());
			// MOSTRAR FECHA POR DIA, MES Y ANIO
			String anio = String.valueOf(calendar.get(Calendar.YEAR));
			String mes = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es", "ES"));
			String dia = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			String fechaPrestamoHoy = dia + " de " + mes + " " + anio;
			// OBSERVACIONES
			prestamo.setObservaciones("El usuario: " + prestamo.getUsuario().getNombres() + ", "
					+ prestamo.getUsuario().getApellidos() + "(DNI " + prestamo.getUsuario().getNroDocumento()
					+ ") ha solicitado el libro: " + prestamo.getLibro().getTitulo() + " para el dìa "
					+ fechaPrestamoHoy + ", hasta el dìa " + prestamo.getFecha_devolucion() + ".");
			// DEVOLUCION
			prestamo.setDevolucion(false);
			prestamoService.save(prestamo);
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
		}
		return "/usuarios/biblioteca/solicitarLibro";
	}

	/*
	 * QUERY SIMPLE PARA MOSTRAR EN LA TABLA DE DISPONIBILIDAD LIBRO SELECT
	 * lo.direccion AS LOCAL, li.estado AS DISPONIBLE, li.stock AS STOCK FROM
	 * biblioteca2020.libros li INNER JOIN biblioteca2020.locales lo ON li.local_id
	 * = lo.id WHERE li.titulo like 'Las noches Blancas';
	 */
	/*
	 * QUERY COMPLETA PARA LLAMAR DESDE LA BD SELECT
	 *
	 * FROM biblioteca2020.libros li INNER JOIN biblioteca2020.locales lo ON
	 * li.local_id = lo.id WHERE li.titulo like 'Las noches Blancas';
	 */

	@GetMapping("/crearPerfil")
	public String perfil(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("roles", roleService.findOnlyUsers());
		model.addAttribute("titulo", "Registro de Usuario");
		return "/usuarios/perfil";
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cancelarPerfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	@PostMapping(value = "/crearPerfil")
	public String crearPerfil(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash
	// , @RequestParam("foto_usu") MultipartFile foto
	) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/perfil";
		}
		/* VALIDACIÓN EMAIL */
		Usuario usuarioExistente = usuarioService.findByEmailIgnoreCase(usuario.getEmail());
		if (usuarioExistente != null) {
			model.addAttribute("error", "El correo ya está asociado a otro usuario!");
			return "/usuarios/perfil";
		} else {
			/* LÓGICA DE REGISTRO DE USUARIOS */
			/*
			 * if (!foto.isEmpty()) { Path directorioRecursos =
			 * Paths.get("src//main//resources//static/uploads"); String rootPath =
			 * directorioRecursos.toFile().getAbsolutePath(); try { byte[] bytes =
			 * foto.getBytes(); Path rutaCompleta = Paths.get(rootPath + "//" +
			 * foto.getOriginalFilename()); Files.write(rutaCompleta, bytes);
			 * usuario.setFoto_usuario(foto.getOriginalFilename()); } catch (IOException e)
			 * { model.addAttribute("error",
			 * "Lo sentimos, hubo un error a la hora de cargar tu foto"); } }
			 */
			/*
			 * else if (usuario.getFoto_usuario() == null || usuario.getFoto_usuario() ==
			 * "") { usuario.setFoto_usuario("no-image.jpg"); }
			 */

			usuario.setFoto_usuario("no-image.jpg");

			try {
				usuarioService.save(usuario);
				/*
				 * REGISTRO EL TOKEN DE REGISTRO SEGUN EL CORREO DEL USUARIO, PARA SU VALIDACIÒN
				 */
				/*
				 * ConfirmationToken confirmationToken = new ConfirmationToken(usuario);
				 * confirmationTokenRepository.save(confirmationToken); // ENVÌO DEL CORREO DE
				 * VALIDACIÒN SimpleMailMessage mailMessage = new SimpleMailMessage();
				 * mailMessage.setTo(usuario.getEmail());
				 * mailMessage.setSubject("Completar Registro | Biblioteca2020");
				 * mailMessage.setFrom("edmech25@gmail.com"); mailMessage.setText(
				 * "Buenas noches, hemos recibido tu peticiòn de registro a Biblioteca2020. Para confirmar tu cuenta, entrar aquì: "
				 * + "http://localhost:8080/usuarios/cuenta-verificada?token=" +
				 * confirmationToken.getConfirmationToken()); flash.addFlashAttribute("success",
				 * "El usuario ha sido registrado en la base de datos.");
				 * emailSenderService.sendEmail(mailMessage); model.addAttribute("titulo",
				 * "Registro exitoso"); model.addAttribute("email", usuario.getEmail()); return
				 * "/usuarios/registro-exitoso";
				 */
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
			// ENVÌO DEL CORREO DE VALIDACIÒN
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(usuario.getEmail());
			mailMessage.setSubject("Completar Registro | Biblioteca2020");
			mailMessage.setFrom("edmech25@gmail.com");
			mailMessage.setText(
					"Buenas noches, hemos recibido tu peticiòn de registro a Biblioteca2020. Para confirmar tu cuenta, entrar aquì: "
							+ "http://localhost:8080/usuarios/cuenta-verificada?token="
							+ confirmationToken.getConfirmationToken());
			flash.addFlashAttribute("success", "El usuario ha sido registrado en la base de datos.");
			emailSenderService.sendEmail(mailMessage);
			model.addAttribute("titulo", "Registro exitoso");
			model.addAttribute("email", usuario.getEmail());
			return "/usuarios/registro-exitoso";

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
				model.addAttribute("titulo", "Error al Registrar");
				return "/usuarios/error-registro";
			}
		} else {
			model.addAttribute("error", "El enlace es invàlido!");
			return "/usuarios/error-registro";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/editarPerfil")
	public String editarPerfil(Map<String, Object> modelMap, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Usuario usuario = usuarioService.findByUsernameAndEstado(userDetails.getUsername(), true);
		modelMap.put("editable", true);
		modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Modificar Usuario");
		try {
			modelMap.put("usuario", usuario);
			return "/usuarios/perfil";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/editarPerfil")
	public String guardarPerfil(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			return "/usuarios/perfil";
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
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
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/cambioPassword")
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
	@PostMapping("/cambioPassword")
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
			System.out.println(result);
			model.addAttribute("titulo", "Cambiar Password");
			return "/usuarios/cambio-password";
		}
		try {
			usuarioService.cambiarPassword(cambiarPassword);
			flash.addFlashAttribute("success", "Password Actualizada");
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/usuarios/cambio-password";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/deshabilitarPerfil")
	public String deshabilitarPerfil(RedirectAttributes flash, Authentication authentication) {
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
			usuario.setEstado(false);
			usuarioService.update(usuario);
			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL USUARIO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			return "redirect:/login";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/perfil";
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@GetMapping("/crear")
	public String crearFormUsuario(Map<String, Object> modelMap) {
		modelMap.put("usuario", new Usuario());
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Registro de Usuario");
		return "usuarios/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@PostMapping(value = "/crear")
	public String crearUsuario(@Valid Usuario usuario, BindingResult result, Model model, Map<String, Object> modelMap,
			SessionStatus status, RedirectAttributes flash, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Registro de Usuario");
			return "/usuarios/crear";
		}

		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
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
	@GetMapping("/editar/{id}")
	public String editarFormUsuario(@PathVariable(value = "id") Long id, Map<String, Object> modelMap,
			RedirectAttributes flash) {
		Usuario usuario = null;
		modelMap.put("editable", true);
		modelMap.put("passwordForm", new CambiarPassword(id));
		modelMap.put("roles", roleService.findOnlyUsers());
		modelMap.put("titulo", "Modificar Usuario");
		try {
			usuario = usuarioService.findById(id);
			modelMap.put("usuario", usuario);
			return "/usuarios/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String guardarUsuario(@Valid Usuario usuario, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_usu") MultipartFile foto) {
		if (result.hasErrors()) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Usuario");
			return "/usuarios/editar";
		}

		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
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
			usuarioService.update(usuario);
			flash.addFlashAttribute("warning",
					"El usuario con código " + usuario.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/usuarios/listar";
		} catch (Exception e) {
			model.addAttribute("usuario", usuario);
			model.addAttribute("editable", true);
			modelMap.put("passwordForm", new CambiarPassword(usuario.getId()));
			model.addAttribute("roles", roleService.findOnlyUsers());
			model.addAttribute("titulo", "Modificar Usuario");
			model.addAttribute("error", e.getMessage());
			return "/usuarios/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarUsuario(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Usuario usuario = null;
		try {
			usuario = usuarioService.findById(id);
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
