package com.biblioteca2020.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.dto.CambiarPassword;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.EmpleadoLog;
import com.biblioteca2020.models.service.IEmpleadoLogService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILocalService;
import com.biblioteca2020.models.service.IRoleService;

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

	private static final List<String> formatosFoto = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	// ############################## ROLE EMPLEADO, ROLE ADMIN
	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/editar-perfil")
	public String editarPerfil(Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Modificar Perfil");
		model.addAttribute("editable", true);
		model.addAttribute("empleado", empleado);
		// AQUÍ PREGUNTO QUE ROL TIENE EL USUARIO REGISTRADO
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_ADMIN]":
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				break;
			case "[ROLE_EMPLEADO]":
				model.addAttribute("roles", roleService.findOnlyEmpleados());
				break;
			case "[ROLE_SYSADMIN]":
				model.addAttribute("roles", roleService.findForSysadmin());
				break;
		}
		return "/empleados/perfil";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@PostMapping(value = "/editar-perfil")
	public String guardarPerfil(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Map<String, Object> modelMap, @RequestParam("foto_emp") MultipartFile foto,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_ADMIN]":
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					break;
				case "[ROLE_EMPLEADO]":
					model.addAttribute("roles", roleService.findOnlyEmpleados());
					break;
				case "[ROLE_SYSADMIN]":
					model.addAttribute("roles", roleService.findForSysadmin());
					break;
			}
			return "/empleados/perfil";
		}

		// PREGUNTO SI EL PARAMETRO ES NULO
		if (!foto.isEmpty()) {
			// PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
			if (formatosFoto.contains(foto.getContentType())) {
				String rootPath = "C://Temp//uploads";
				try {
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} catch (IOException e) {
					model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
					model.addAttribute("titulo", "Modificar Perfil");
					model.addAttribute("empleado", empleado);
					model.addAttribute("editable", true);
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_ADMIN]":
							model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
							break;
						case "[ROLE_EMPLEADO]":
							model.addAttribute("roles", roleService.findOnlyEmpleados());
							break;
						case "[ROLE_SYSADMIN]":
							model.addAttribute("roles", roleService.findForSysadmin());
							break;
					}
					return "/empleados/perfil";
				}
			} else {
				model.addAttribute("error", "El formato de la foto es incorrecto");
				model.addAttribute("titulo", "Modificar Perfil");
				model.addAttribute("empleado", empleado);
				model.addAttribute("editable", true);
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_ADMIN]":
						model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
						break;
					case "[ROLE_EMPLEADO]":
						model.addAttribute("roles", roleService.findOnlyEmpleados());
						break;
					case "[ROLE_SYSADMIN]":
						model.addAttribute("roles", roleService.findForSysadmin());
						break;
				}
				return "/empleados/perfil";
			}
		}

		try {
			Empleado empleadoOld = empleado;
			empleadoService.update(empleado);

			// AL CAMBIAR PASSWORD, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(),
					"UPDATE PERFIL BY EMPLEADO", null, new Date(), null));

			flash.addFlashAttribute("warning", "Perfil actualizado.");
			status.setComplete();
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("titulo", "Modificar Perfil");
			model.addAttribute("editable", true);
			model.addAttribute("empleado", empleado);
			model.addAttribute("error", e.getMessage());
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_ADMIN]":
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					break;
				case "[ROLE_EMPLEADO]":
					model.addAttribute("roles", roleService.findOnlyEmpleados());
					break;
				case "[ROLE_SYSADMIN]":
					model.addAttribute("roles", roleService.findForSysadmin());
					break;
			}
			return "/empleados/perfil";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/cancelar-perfil")
	public String cancelarPerfil() {
		return "redirect:/home";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping("/cambio-password")
	public String cambioPasswordEmpleado(Model model, Authentication authentication) {
		CambiarPassword cambiarPassword = new CambiarPassword();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		cambiarPassword.setId(empleado.getId());
		model.addAttribute("cambiarPassword", cambiarPassword);
		model.addAttribute("titulo", "Cambiar Password");
		return "/empleados/cambio-password";
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@PostMapping("/cambio-password")
	public String cambioPasswordEmpleado(@Valid CambiarPassword cambiarPassword, BindingResult resultForm, Model model,
			RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoOld = empleado;
		if (resultForm.hasErrors()) {
			// CON ESTE BLOQUE SOBREESCRIBO EL ERROR GENÈRICO "NO PUEDE ESTAR VACÍO"
			if (cambiarPassword.getPasswordActual().equals("") || cambiarPassword.getNuevaPassword().equals("")
					|| cambiarPassword.getConfirmarPassword().equals("")) {
				model.addAttribute("cambiarPasswordError", "Todos los campos son obligatorios");
				model.addAttribute("titulo", "Cambiar Password");
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
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(),
					"CHANGE PASSWORD BY EMPLEADO", null, new Date(), null));

			flash.addFlashAttribute("success", "Password Actualizada");
			return "redirect:/home";
		} catch (Exception e) {
			model.addAttribute("titulo", "Cambiar Password");
			model.addAttribute("cambiarPassword", cambiarPassword);
			model.addAttribute("cambiarPasswordError", e.getMessage());
			return "/empleados/cambio-password";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_EMPLEADO', 'ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar-perfil")
	public String deshabilitarPerfil(RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoOld = empleado;

		try {
			empleado.setEstado(false);
			empleadoService.update(empleado);

			// AL DESHABILITAR EL PERFIL, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(),
					"LOCK ACCOUNT BY EMPLEADO", null, new Date(), null));

			flash.addFlashAttribute("info", "Su cuenta ha sido deshabilitada.");
			// CON ESTA PROPIEDAD ELIMINO LA SESIÓN DEL EMPLEADO LOGUEADO, PARA PODERLO
			// REDIRECCIONAR AL LOGIN
			authentication.setAuthenticated(false);
			return "redirect:/login";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/perfil";
		}
	}

	// ############################## ROLE ADMIN
	// LISTADO DE EMPLEADOS
	// SI SOY ADMIN, VEO MI REGISTRO
	// SI NO, LO OCULTO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/listar")
	public String listarEmpleados(Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.addAttribute("empleados",
				empleadoService.fetchByIdWithLocalWithEmpresa(empleado.getLocal().getId()));
		model.addAttribute("empleado", new Empleado());
		model.addAttribute("titulo",
				"Listado de Empleados de '" + empleado.getLocal().getEmpresa().getRazonSocial() + "'");
		return "/empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/empleados/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/crear")
	public String crearFormEmpleado(Model model, Authentication authentication, RedirectAttributes flash) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		try {
			model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			Empleado emp = new Empleado();
			emp.setFoto_empleado("no-image.jpg");
			model.addAttribute("empleado", emp);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de Empleado");
			return "empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String crearEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_emp") MultipartFile foto, Authentication authentication) {
		if (result.hasErrors()) {
			model.addAttribute("empleado", empleado);
			try {
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de Empleado");
			return "/empleados/crear";
		}

		if (!foto.isEmpty()) {
			String rootPath = "C://Temp//uploads";
			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				empleado.setFoto_empleado(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			}
		}

		// PREGUNTO SI EL PARAMETRO ES NULO
		if (!foto.isEmpty()) {
			// PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
			if (formatosFoto.contains(foto.getContentType())) {
				String rootPath = "C://Temp//uploads";
				try {
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} catch (IOException e) {
					model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
					model.addAttribute("empleado", empleado);
					try {
						model.addAttribute("localesList",
								localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
					} catch (Exception e1) {
						flash.addFlashAttribute("error", e1.getMessage());
						return "/empleados/crear";
					}
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					model.addAttribute("titulo", "Registro de Empleado");
					return "/empleados/crear";
				}
			} else {
				model.addAttribute("error", "El formato de la foto es incorrecto");
				model.addAttribute("empleado", empleado);
				try {
					model.addAttribute("localesList",
							localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
				} catch (Exception e2) {
					flash.addFlashAttribute("error", e2.getMessage());
					return "/empleados/crear";
				}
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				model.addAttribute("titulo", "Registro de Empleado");
				return "/empleados/crear";
			}
		} else {
			empleado.setFoto_empleado("no-image.jpg");
		}

		try {
			empleadoService.save(empleado);

			// AL CREAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE EMPLEADOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			empleadoLogService.save(new EmpleadoLog(idRole, empleado.getNombres(), null, empleado.getApellidos(), null,
					empleado.getNroDocumento(), null, empleado.getDireccion(), null, empleado.getEmail(), null,
					empleado.getCelular(), null, empleado.getFecha_registro(), null, empleado.getUsername(), null,
					empleado.getPassword(), null, empleado.getEstado(), null, empleado.getFoto_empleado(), null,
					"INSERT BY ADMIN", new Date(), null, null));

			flash.addFlashAttribute("success",
					"El empleado ha sido registrado en la base de datos (Código " + empleado.getId() + ")");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
			} catch (Exception e2) {
				flash.addFlashAttribute("error", e2.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Registro de Empleado");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/editar/{id}")
	public String editarFormEmpleado(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		// AQUI VALIDO SI EL USUARIO LOGUEADO ES ADMIN Y PUEDE EDITAR SU MISMO REGISTRO
		try {
			Empleado empleadoAdmin = empleadoService.findById(id);
			if (empleadoAdmin.getRoles().toString().contains("ROLE_ADMIN") == true
					&& empleado.getRoles().toString().contains("ROLE_ADMIN") == false) {
				flash.addFlashAttribute("error", "No tienes permiso para acceder a este recurso.");
				return "redirect:/empleados/listar";
			}
		} catch (Exception e1) {
			flash.addFlashAttribute("error", e1.getMessage());
			return "redirect:/empleados/listar";
		}
		// FIN VALIDACION ADMIN
		try {
			model.addAttribute("localesList", localService.findFirstByEmpresa(empleado.getLocal().getEmpresa()));
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
		model.addAttribute("editable", true);
		model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
		model.addAttribute("titulo", "Modificar Empleado");
		try {
			Empleado empleadoEditable = empleadoService.findById(id);
			model.addAttribute("empleado", empleadoEditable);
			return "/empleados/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, Authentication authentication, @RequestParam("foto_emp") MultipartFile foto) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleadoLogueado = empleadoService.findByUsername(userDetails.getUsername());
		Empleado empleadoOld = empleadoLogueado;
		if (result.hasErrors()) {
			try {
				model.addAttribute("localesList",
						localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Modificar Empleado");
			return "/empleados/editar";
		}

		// PREGUNTO SI EL PARAMETRO ES NULO
		if (!foto.isEmpty()) {
			// PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
			if (formatosFoto.contains(foto.getContentType())) {
				String rootPath = "C://Temp//uploads";
				try {
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					empleado.setFoto_empleado(foto.getOriginalFilename());
				} catch (IOException e) {
					model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
					try {
						model.addAttribute("localesList",
								localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
					} catch (Exception e2) {
						flash.addFlashAttribute("error", e2.getMessage());
						return "/empleados/crear";
					}
					model.addAttribute("empleado", empleado);
					model.addAttribute("editable", true);
					model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
					model.addAttribute("titulo", "Modificar Empleado");
					return "/empleados/editar";
				}
			} else {
				model.addAttribute("error", "El formato de la foto es incorrecto");
				try {
					model.addAttribute("localesList",
							localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
				} catch (Exception e3) {
					flash.addFlashAttribute("error", e3.getMessage());
					return "/empleados/crear";
				}
				model.addAttribute("empleado", empleado);
				model.addAttribute("editable", true);
				model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
				model.addAttribute("titulo", "Modificar Empleado");
				return "/empleados/editar";
			}
		}

		try {
			empleadoService.update(empleado);

			// AL ACTUALIZAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(), "UPDATE BY ADMIN",
					null, new Date(), null));

			flash.addFlashAttribute("warning",
					"El empleado con código " + empleado.getId() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			try {
				model.addAttribute("localesList",
						localService.findFirstByEmpresa(empleadoLogueado.getLocal().getEmpresa()));
			} catch (Exception e2) {
				flash.addFlashAttribute("error", e2.getMessage());
				return "/empleados/crear";
			}
			model.addAttribute("empleado", empleado);
			model.addAttribute("editable", true);
			model.addAttribute("roles", roleService.findForEmpleadosAndAdmin());
			model.addAttribute("titulo", "Modificar Empleado");
			model.addAttribute("error", e.getMessage());
			return "/empleados/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/deshabilitar/{id}")
	public String deshabilitarEmpleado(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		try {
			Empleado empleado = empleadoService.findById(id);
			Empleado empleadoOld = empleado;
			empleado.setEstado(false);
			empleadoService.update(empleado);

			// AL DESHABILITAR UN EMPLEADO, INSERTO MI REGISTRO EN EL LOG DE USUARIOS
			Long idRole = empleado.getRoles().iterator().next().getId();
			empleadoLogService.save(new EmpleadoLog(idRole, empleadoOld.getNombres(), empleado.getNombres(),
					empleadoOld.getApellidos(), empleado.getApellidos(), empleadoOld.getNroDocumento(),
					empleado.getNroDocumento(), empleadoOld.getDireccion(), empleado.getDireccion(),
					empleadoOld.getEmail(), empleado.getEmail(), empleadoOld.getCelular(), empleado.getCelular(),
					empleadoOld.getFecha_registro(), empleado.getFecha_registro(), empleadoOld.getUsername(),
					empleado.getUsername(), empleadoOld.getPassword(), empleado.getPassword(), empleadoOld.getEstado(),
					empleado.getEstado(), empleadoOld.getFoto_empleado(), empleado.getFoto_empleado(),
					"LOCK ACCOUNT BY ADMIN", null, new Date(), null));

			flash.addFlashAttribute("info", "El empleado con código " + empleado.getId() + " ha sido deshabilitado.");
			return "redirect:/empleados/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/empleados/listar";
		}
	}
}
