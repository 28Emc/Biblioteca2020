package com.biblioteca2020.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.EmpleadoLog;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.IEmpleadoLogService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILocalService;
import com.biblioteca2020.models.service.IRoleService;
import com.biblioteca2020.view.pdf.GenerarReportePDF;
import com.biblioteca2020.view.xlsx.GenerarReporteExcel;

@Controller
@RequestMapping("/empleados")
@SessionAttributes("empleado")
public class EmpleadoController {

	@Autowired
	private IEmpleadoService empleadoService;

	@Autowired
	private IEmpleadoLogService empleadoLogService;

	@Autowired
	private ILocalService localService;

	@Autowired
	private IRoleService roleService;

	// LISTADO DE FORMATOS DE FOTO PERMITIDOS
	private static final List<String> formatosFoto = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	// ############################# CRUD #############################
	// FORMULARIO DE EDICIÓN DE PERFIL DEL EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/editar-perfil")
	public String editarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Modificar datos del perfil");
		model.addAttribute("editable", true);
		model.addAttribute("empleado", empleado);
		// AQUÍ PREGUNTO QUE ROL TIENE EL USUARIO REGISTRADO
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				model.addAttribute("roles", roleService.findForSysadmin());
				break;
			case "[ROLE_ADMIN]":
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				break;
			case "[ROLE_EMPLEADO]":
				model.addAttribute("roles", roleService.findOnlyEmpleados());
				break;
		}
		return "/empleados/perfil";
	}

	// ACTUALIZAR PERFIL DEL EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@PostMapping("/editar-perfil")
	public String actualizarPerfil(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_emp") MultipartFile foto,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String tipoOp = "";
		try {
			// CAPTURAR ERRORES DE FORMULARIO
			if (result.hasErrors()) {
				model.addAttribute("titulo", "Modificar datos del perfil");
				model.addAttribute("empleado", empleado);
				model.addAttribute("editable", true);
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						model.addAttribute("roles", roleService.findForSysadmin());
						break;
					case "[ROLE_ADMIN]":
						model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
						break;
					case "[ROLE_EMPLEADO]":
						model.addAttribute("roles", roleService.findOnlyEmpleados());
						break;
				}
				return "/empleados/perfil";
			}
			// PREGUNTO SI EL PARAMETRO DE LA FOTO ES NULO ..
			if (!foto.isEmpty()) {
				// .. Y PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN CORRECTO
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} else {
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("titulo", "Modificar datos del perfil");
					model.addAttribute("empleado", empleado);
					model.addAttribute("editable", true);
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_SYSADMIN]":
							model.addAttribute("roles", roleService.findForSysadmin());
							break;
						case "[ROLE_ADMIN]":
							model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
							break;
						case "[ROLE_EMPLEADO]":
							model.addAttribute("roles", roleService.findOnlyEmpleados());
							break;
					}
					return "/empleados/perfil";
				}
			}
			// EXCEPCION DE FOTO
		} catch (IOException e) {
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("titulo", "Modificar datos del perfil");
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					model.addAttribute("roles", roleService.findForSysadmin());
					break;
				case "[ROLE_ADMIN]":
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					break;
				case "[ROLE_EMPLEADO]":
					model.addAttribute("roles", roleService.findOnlyEmpleados());
					break;
			}
			return "/empleados/perfil";
		}
		try {
			Empleado empleadoOld = empleado;
			// AQUI VALIDO SI EL EMPLEADO CAMBIÓ SU USERNAME, SI LO HIZO, SOLICITO AL
			// EMPLEADO QUE ENTRE NUEVAMENTE AL SISTEMA PARA ACTUALIZAR SUS
			// CREDENCIALES;
			// EN CASO CONTRARIO OBTIENE UN ERROR AL MOMENTO DE ACCEDER A CUALQUIER OTRO
			// RECURSO.
			if (!userDetails.getUsername().equalsIgnoreCase(empleado.getUsername())) {
				empleadoService.update(empleado);
				authentication.setAuthenticated(false);
				flash.addFlashAttribute("info", "Perfil actualizado! Iniciar sesión con sus nuevas credenciales!");
				return "redirect:/login";
			} else {
				empleadoService.update(empleado);
				// AL CAMBIAR PASSWORD, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
				Long idRole = empleado.getRoles().iterator().next().getId();
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						tipoOp = "UPDATE ACCOUNT SYSADMIN";
						break;
					case "[ROLE_ADMIN]":
						tipoOp = "UPDATE ACCOUNT ADMIN";
						break;
					case "[ROLE_EMPLEADO]":
						tipoOp = "UPDATE ACCOUNT EMPLEADO";
						break;
				}
				empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
						empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
						empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
						empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
						empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
						empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(),
						empleadoOld.getEstado(), empleado.getEstado(), empleadoOld.getFoto_empleado(),
						empleado.getFoto_empleado(), tipoOp, null, new Date(), null));
				flash.addFlashAttribute("warning", "Perfil actualizado!");
				status.setComplete();
				return "redirect:/home";
			}
		} catch (Exception e) {
			model.addAttribute("titulo", "Modificar datos del perfil");
			model.addAttribute("editable", true);
			model.addAttribute("empleado", empleado);
			model.addAttribute("error", e.getMessage());
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					model.addAttribute("roles", roleService.findForSysadmin());
					break;
				case "[ROLE_ADMIN]":
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					break;
				case "[ROLE_EMPLEADO]":
					model.addAttribute("roles", roleService.findOnlyEmpleados());
					break;
			}
			return "/empleados/perfil";
		}
	}

	// REGRESAR A HOME DESDE EL PERFIL DE EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/cancelar-perfil")
	public String salirPerfil() {
		return "redirect:/home";
	}

	// FORMULARIO DE CAMBIO DE CONTRASEÑA DEL EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/cambio-password")
	public String editarPasswordEmpleado(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(empleado.getId());
		model.addAttribute("cambiarPassword", cambiarPassword);
		model.addAttribute("titulo", "Cambio de contraseña");
		return "/empleados/cambio-password";
	}

	// ACTUALIZACIÓN DE CONTRASEÑA DEL EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@PostMapping("/cambio-password")
	public String actualizarPasswordEmpleado(@Valid CambiarPassword cambiarPassword, BindingResult resultForm,
			Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoOld = empleado;
		String tipoOp = "";
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getPasswordActual().equals("") || cambiarPassword.getNuevaPassword().equals("")
					|| cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios!");
				model.addAttribute("titulo", "Cambio de contraseña");
				return "/empleados/cambio-password";
			}
			String result = resultForm.getAllErrors().stream().map(x -> x.getDefaultMessage())
					.collect(Collectors.joining(", "));
			model.addAttribute("cambiarPasswordError", result);
			return "/empleados/cambio-password";
		}
		try {
			empleadoService.cambiarPassword(cambiarPassword);
			// AL CAMBIAR PASSWORD, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "CHANGE PASSWORD SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "CHANGE PASSWORD ADMIN";
					break;
				case "[ROLE_EMPLEADO]":
					tipoOp = "CHANGE PASSWORD EMPLEADO";
					break;
			}
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(), tipoOp, null,
					new Date(), null));
			flash.addFlashAttribute("success", "Contraseña actualizada!");
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("titulo", "Cambio de contraseña");
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/empleados/cambio-password";
		}
	}

	// DESHABILITAR PERFIL DEL EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/deshabilitar-perfil")
	public String deshabilitarPerfil(RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoOld = empleado;
		String ruta = "";
		String tipoOp = "";
		try {
			empleado.setEstado(false);
			empleadoService.update(empleado);
			// AL DESHABILITAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "ACCOUNT LOCKED SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "ACCOUNT LOCKED ADMIN";
					break;
				case "[ROLE_EMPLEADO]":
					tipoOp = "ACCOUNT LOCKED EMPLEADO";
					break;
			}
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(), tipoOp, null,
					new Date(), null));
			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL EMPLEADO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			ruta = "redirect:/login";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			ruta = "redirect:/empleados/perfil";
		}
		return ruta;
	}

	// LISTADO DE EMPLEADOS
	// SI SOY ADMIN, VEO MI REGISTRO Y LOS DE MI LOCAL
	// SI SOY SYSADMIN, LO OCULTO Y VEO TODOS LOS EMPLEADOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/listar")
	public String listarEmpleados(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				List<Empleado> empleados = empleadoService.findAll();
				for (int i = 0; i < empleados.size(); i++) {
					// RETIRO TODOS LOS EMPLEADOS CON ROLE PRUEBA Y DEJO LOS DEMÀS
					empleados.removeIf(n -> n.getRoles().iterator().next().getAuthority().equals("ROLE_PRUEBA"));
				}
				model.addAttribute("titulo", "Listado de empleados");
				model.addAttribute("empleados", empleados);
				break;
			case "[ROLE_ADMIN]":
				// EMPLEADOS DEL MISMO LOCAL DEL ADMIN
				model.addAttribute("empleados",
						empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId()));
				model.addAttribute("titulo",
						"Listado de empleados de " + empleado.getLocal().getEmpresa().getRazonSocial());
				break;
		}
		model.addAttribute("empleado", new Empleado());
		return "/empleados/listar";
	}

	// REGRESAR A LISTADO DE EMPLEADOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/empleados/listar";
	}

	// FORMULARIO DE REGISTRO DE EMPLEADO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/crear")
	public String crearFormEmpleado(Model model, Authentication authentication, RedirectAttributes flash) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoForm = new Empleado();
		empleadoForm.setFoto_empleado("no-image.jpg");
		List<Local> localesList = null;
		String ruta = "";
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("localesList", localesList);
			model.addAttribute("empleado", empleadoForm);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de empleado nuevo");
			ruta = "empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			ruta = "redirect:/empleados/listar";
		}
		return ruta;
	}

	// REGISTRO DE EMPLEADO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_emp") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoLog = empleadoService.findByUsername(userDetails.getUsername());
		List<Local> localesList = null;
		String tipoOp = "";
		try {
			if (result.hasErrors()) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						localesList = localService.findAll();
						break;
					case "[ROLE_ADMIN]":
						localesList = localService.findFirstByEmpresa(empleadoLog.getLocal().getEmpresa());
						break;
				}
				model.addAttribute("empleado", empleado);
				model.addAttribute("localesList", localesList);
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				model.addAttribute("titulo", "Registro de empleado nuevo");
				return "/empleados/crear";
			}
			// PREGUNTO SI EL PARAMETRO ES NULO
			if (!foto.isEmpty()) {
				// PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} else {
					model.addAttribute("error", "El formato de la foto es incorrecto");
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_SYSADMIN]":
							localesList = localService.findAll();
							break;
						case "[ROLE_ADMIN]":
							localesList = localService.findFirstByEmpresa(empleadoLog.getLocal().getEmpresa());
							break;
					}
					model.addAttribute("empleado", empleado);
					model.addAttribute("localesList", localesList);
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					model.addAttribute("titulo", "Registro de empleado nuevo");
					return "/empleados/crear";
				}
			} else {
				empleado.setFoto_empleado("no-image.jpg");
			}
		} catch (IOException e) {
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleadoLog.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("localesList", localesList);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de empleado nuevo");
			return "/empleados/crear";
		}
		try {
			empleadoService.save(empleado);
			// AL CREAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE EMPLEADOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "INSERT BY SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "INSERT BY ADMIN";
					break;
				case "[ROLE_EMPLEADO]":
					tipoOp = "INSERT BY EMPLEADO";
					break;
			}
			empleadoLogService.save(new EmpleadoLog(idRole, empleado.getNombres(), null, empleado.getApellidos(), null,
					empleado.getNroDocumento(), null, empleado.getDireccion(), null, empleado.getEmail(), null,
					empleado.getCelular(), null, empleado.getFecha_registro(), null, empleado.getUsername(), null,
					empleado.getPassword(), null, empleado.getEstado(), null, empleado.getFoto_empleado(), null, tipoOp,
					new Date(), null, null));
			flash.addFlashAttribute("success",
					"El empleado ha sido registrado en la base de datos (Código " + empleado.getId() + ")");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleadoLog.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("localesList", localesList);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de empleado nuevo");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	// FORMULARIO DE EDICIÓN DE EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/editar/{id}")
	public String editarEmpleado(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Local> localesList = null;
		String ruta = "";
		try {
			Empleado empleadoEditable = empleadoService.findById(id);
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("localesList", localesList);
			model.addAttribute("editable", true);
			model.addAttribute("empleado", empleadoEditable);
			model.addAttribute("titulo", "Modificar datos del empleado");
			ruta = "/empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			ruta = "redirect:/empleados/listar";
		}
		return ruta;
	}

	// ACTUALIZAR EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication, @RequestParam("foto_emp") MultipartFile foto) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoLogueado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoLog = empleadoLogueado;
		List<Local> localesList = null;
		String tipoOp = "";
		try {
			if (result.hasErrors()) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						localesList = localService.findAll();
						break;
					case "[ROLE_ADMIN]":
						localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
						break;
				}
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				model.addAttribute("localesList", localesList);
				model.addAttribute("editable", true);
				model.addAttribute("empleado", empleado);
				model.addAttribute("titulo", "Modificar datos del empleado");
				return "/empleados/crear";
			}
			// PREGUNTO SI EL PARAMETRO ES NULO ..
			if (!foto.isEmpty()) {
				// ..Y PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN CORRECTO
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} else {
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_SYSADMIN]":
							localesList = localService.findAll();
							break;
						case "[ROLE_ADMIN]":
							localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
							break;
					}
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("localesList", localesList);
					model.addAttribute("editable", true);
					model.addAttribute("empleado", empleado);
					model.addAttribute("titulo", "Modificar datos del empleado");
					return "/empleados/crear";
				}
			}
		} catch (IOException e) {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("localesList", localesList);
			model.addAttribute("editable", true);
			model.addAttribute("empleado", empleado);
			model.addAttribute("titulo", "Modificar datos del empleado");
			return "/empleados/crear";
		}
		try {
			empleadoService.update(empleado);
			// AL ACTUALIZAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "UPDATE BY SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "UPDATE BY ADMIN";
					break;
			}
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoLog.getNombres(), empleado.getNombres(),
					empleadoLog.getApellidos(), empleado.getApellidos(), empleadoLog.getNroDocumento(),
					empleado.getNroDocumento(), empleadoLog.getDireccion(), empleado.getDireccion(),
					empleadoLog.getEmail(), empleado.getEmail(), empleadoLog.getCelular(), empleado.getCelular(),
					empleadoLog.getFecha_registro(), empleado.getFecha_registro(), empleadoLog.getUsername(),
					empleado.getUsername(), empleadoLog.getPassword(), empleado.getPassword(), empleadoLog.getEstado(),
					empleado.getEstado(), empleadoLog.getFoto_empleado(), empleado.getFoto_empleado(), tipoOp, null,
					new Date(), null));
			flash.addFlashAttribute("warning",
					"El empleado con código " + empleado.getId() + " ha sido actualizado en la base de datos");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					localesList = localService.findAll();
					break;
				case "[ROLE_ADMIN]":
					localesList = localService.findFirstByEmpresa(empleado.getLocal().getEmpresa());
					break;
			}
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("localesList", localesList);
			model.addAttribute("editable", true);
			model.addAttribute("empleado", empleado);
			model.addAttribute("titulo", "Modificar datos del empleado");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	// DESHABILITAR EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarEmpleado(@PathVariable(value = "id") Long id, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String tipoOp = "";
		try {
			Empleado empleado = empleadoService.findById(id);
			Empleado empleadoOld = empleado;
			empleado.setEstado(false);
			empleadoService.update(empleado);
			Long idRole = empleado.getRoles().iterator().next().getId();
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					tipoOp = "LOCK ACCOUNT BY SYSADMIN";
					break;
				case "[ROLE_ADMIN]":
					tipoOp = "LOCK ACCOUNT BY ADMIN";
					break;
			}
			// AL DESHABILITAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(), tipoOp, null,
					new Date(), null));
			flash.addFlashAttribute("info", "El empleado con código " + empleado.getId() + " ha sido deshabilitado");
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	// ############################# REPORTES #############################
	// FORMULARIO DE REPORTES DE EMPLEADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/reportes")
	public String crearReporte(Model model, Authentication authentication) {
		model.addAttribute("titulo", "Creación de reportes");
		model.addAttribute("empleado", new Empleado());
		ArrayList<Boolean> estados = new ArrayList<Boolean>();
		estados.add(true);
		estados.add(false);
		model.addAttribute("estados", estados);
		return "/empleados/crear_reporte";
	}

	// REGRESAR A LISTADO DE EMPLEADOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/empleados/cancelar")
	public String salirReporte() {
		return "redirect:/empleados/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE LOCALES ACTIVOS POR SU DIRECCION Y ESTADO
	// MEDIANTE AUTOCOMPLETADO
	@RequestMapping(value = "/cargar-locales-reporte/{term}", produces = { "application/json" })
	public @ResponseBody List<Local> cargarLocalesActivos(@PathVariable String term) {
		List<Local> localesActivos = localService.findByDireccionLikeIgnoreCase("%" + term + "%");
		for (int i = 0; i < localesActivos.size(); i++) {
			localesActivos.removeIf(n -> n.getEstado().equals(false));
		}
		return localesActivos;
	}

	// BUSCAR EMPLEADOS POR LOCAL PARA GENERAR REPORTE
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/reportes/buscar-empleados-por-local", method = RequestMethod.GET)
	public String buscarEmpleadosParaReporte(@RequestParam String buscar_local, Model model,
			Authentication authentication, RedirectAttributes flash) {
		model.addAttribute("titulo", "Reporte Por Local");
		model.addAttribute("enable", true);
		Local local = localService.findByDireccion(buscar_local);
		String ruta = "";
		try {
			if (buscar_local.length() == 0 || buscar_local.length() > 200) {
				flash.addFlashAttribute("error", "Error, el local es incorrecto");
				return "redirect:/empleados/reportes";
			}
			if (local == null) {
				flash.addFlashAttribute("error", "Error, el local no está disponible");
				return "redirect:/empleados/reportes";
			}
			model.addAttribute("local", local);
			ruta = "/empleados/busqueda_empleados_por_local";
		} catch (Exception e) {
			flash.addFlashAttribute("error", "Error, el local es incorrecto o el local no está disponible");
			ruta = "redirect:/empleados/reportes";
		}
		return ruta;
	}

	// GENERAR REPORTE PDF DE EMPLEADOS TOTALES
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/reportes/pdf/empleados-totales", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfEmpleadosTotal(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Empleado> empleados = null;
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				// MOSTRAR TODOS LOS EMPLEADOS
				empleados = empleadoService.findAll();
				break;
			case "[ROLE_ADMIN]":
				// MOSTRAR LOS EMPLEADOS DEL LOCAL DEL ADMIN
				empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
				break;
		}
		for (int i = 0; i < empleados.size(); i++) {
			// QUITAR DEL LISTADO DE EMPLEADOS LOS EMPLEADOS CON ROL PRUEBA
			empleados.removeIf(e -> e.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
		}
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (empleados.size() != 0) {
				bis = GenerarReportePDF.generarPDFEmpleados("Reporte de empleados totales", empleados);
				headers.add("Content-Disposition", "inline; filename=listado-empleados-totales.pdf");
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

	// GENERAR REPORTE PDF DE EMPLEADOS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/reportes/pdf/empleados-por-estado/{estado}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosPorEstado(@PathVariable("estado") String estado,
			Model model, Authentication authentication) {
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		try {
			List<Empleado> empleados = null;
			String titulo = "";
			String tituloPdf = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						empleados = empleadoService.findAll();
						break;
					case "[ROLE_ADMIN]":
						empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
						break;
				}
				// FILTRO SOLAMENTE LOS EMPLEADOS ACTIVOS Y SIN USUARIOS CON ROL PRUEBA
				for (int i = 0; i < empleados.size(); i++) {
					empleados.removeIf(e -> e.getEstado().equals(false));
					empleados.removeIf(
							p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
				}
				titulo = "listado-empleados-disponibles";
				tituloPdf = "Reporte de empleados disponibles";
			} else if (estado.equals("false")) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						empleados = empleadoService.findAll();
						break;
					case "[ROLE_ADMIN]":
						empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
						break;
				}
				// FILTRO SOLAMENTE LOS EMPLEADOS INACTIVOS Y SIN USUARIOS CON ROL PRUEBA
				for (int i = 0; i < empleados.size(); i++) {
					empleados.removeIf(e -> e.getEstado().equals(true));
					empleados.removeIf(
							p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
				}
				titulo = "listado-empleados-no-disponibles";
				tituloPdf = "Reporte de empleados no disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (empleados.size() != 0) {
				bis = GenerarReportePDF.generarPDFEmpleados(tituloPdf, empleados);
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

	// GENERAR PDF DE EMPLEADOS POR LOCAL
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@RequestMapping(value = "/reportes/pdf/empleados-por-local/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfEmpleadosPorLocal(@PathVariable("id") String id,
			Authentication authentication) {
		List<Empleado> empleados = null;
		// GENERO EL REPORTE
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			empleados = empleadoService.fetchByIdWithLocalWithEmpresa(Long.parseLong(id));
			// QUITAR DEL LISTADO DE EMPLEADOS LOS EMPLEADOS CON ROL PRUEBA
			for (int i = 0; i < empleados.size(); i++) {
				empleados.removeIf(p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
			}
			if (empleados.size() != 0) {
				bis = GenerarReportePDF.generarPDFEmpleados("Reporte de empleados por local", empleados);
				headers.add("Content-Disposition", "inline; filename=empleados-por-local-reporte.pdf");
				return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
						.body(new InputStreamResource(bis));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	// GENERAR REPORTE EXCEL DE EMPLEADOS TOTALES
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/reportes/xlsx/empleados-totales", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> generarExcelEmpleadosTotal(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Empleado> empleados = null;
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				// MOSTRAR TODOS LOS EMPLEADOS
				empleados = empleadoService.findAll();
				break;
			case "[ROLE_ADMIN]":
				// MOSTRAR LOS EMPLEADOS DEL LOCAL DEL ADMIN
				empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
				break;
		}
		for (int i = 0; i < empleados.size(); i++) {
			// QUITAR DEL LISTADO DE EMPLEADOS LOS EMPLEADOS CON ROL PRUEBA
			empleados.removeIf(e -> e.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
		}
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (empleados.size() != 0) {
				bis = GenerarReporteExcel.generarExcelEmpleados("Reporte de empleados totales", empleados);
				headers.add("Content-Disposition", "attachment; filename=listado-empleados-totales.xlsx");
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

	// GENERAR REPORTE EXCEL DE EMPLEADOS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/reportes/xlsx/empleados-por-estado/{estado}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repEmpleadosPorEstado(@PathVariable("estado") String estado,
			Authentication authentication) {
		ByteArrayInputStream in;
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Empleado> empleados = null;
		var headers = new HttpHeaders();
		try {
			String titulo = "";
			String tituloExcel = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						empleados = empleadoService.findAll();
						break;
					case "[ROLE_ADMIN]":
						empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
						break;
				}
				// FILTRO SOLAMENTE LOS EMPLEADOS ACTIVOS Y SIN USUARIOS CON ROL PRUEBA
				for (int i = 0; i < empleados.size(); i++) {
					empleados.removeIf(e -> e.getEstado().equals(false));
					empleados.removeIf(
							p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
				}
				titulo = "listado-empleados-disponibles";
				tituloExcel = "Reporte de Empleados Disponibles";
			} else if (estado.equals("false")) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						empleados = empleadoService.findAll();
						break;
					case "[ROLE_ADMIN]":
						empleados = empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId());
						break;
				}
				// FILTRO SOLAMENTE LOS EMPLEADOS INACTIVOS Y SIN USUARIOS CON ROL PRUEBA
				for (int i = 0; i < empleados.size(); i++) {
					empleados.removeIf(e -> e.getEstado().equals(true));
					empleados.removeIf(
							p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
				}
				titulo = "listado-empleados-no-disponibles";
				tituloExcel = "Reporte de Empleados No Disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (empleados.size() != 0) {
				in = GenerarReporteExcel.generarExcelEmpleados(tituloExcel, empleados);
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

	// GENERAR REPORTE EXCEL DE EMPLEADOS POR LOCAL
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@RequestMapping(value = "/reportes/xlsx/empleados-por-local/{id}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repEmpleadosPorLocal(@PathVariable("id") String id) {
		List<Empleado> empleados = null;
		ByteArrayInputStream in;
		var headers = new HttpHeaders();
		try {
			empleados = empleadoService.fetchByIdWithLocalWithEmpresa(Long.parseLong(id));
			// QUITAR DEL LISTADO DE EMPLEADOS LOS EMPLEADOS CON ROL PRUEBA
			for (int i = 0; i < empleados.size(); i++) {
				empleados.removeIf(p -> p.getRoles().iterator().next().getAuthority().toString().equals("ROLE_PRUEBA"));
			}
			if (empleados.size() != 0) {
				in = GenerarReporteExcel.generarExcelEmpleados("Reporte de empleados por local", empleados);
				headers.add("Content-Disposition", "attachment; filename=listado-empleados-por-local.xlsx");
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
}
