package com.biblioteca2020.controllers;

import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.ILocalService;

@Controller
@SessionAttributes("libro")
public class LibroController {

	@Autowired
	private ILibroService libroService;

	@Autowired
	private ILocalService localService;

	@Autowired
	private IEmpleadoService empleadoService;

	/* ************************ SECCIÓN DE SYSADMIN ************************ */
	// LISTADO DE TODOS LOS LIBROS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping("/libros/listar")
	public String listarLibrosAdmin(Model model) {
		List<Libro> libros = libroService.findAll();
		model.addAttribute("libros", libros);
		model.addAttribute("titulo", "Listado de Libros");
		return "/libros/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping("/libros/cancelar")
	public String cancelarAdmin(Model model) {
		List<Libro> libros = libroService.findAll();
		model.addAttribute("libros", libros);
		model.addAttribute("titulo", "Listado de Libros");
		return "redirect:/libros/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping("/libros/crear")
	public String crearLibroAdmin(Map<String, Object> model) {
		List<Local> locales = localService.findAll();
		model.put("locales", locales);
		model.put("libro", new Libro());
		model.put("titulo", "Registro de Libro");
		return "/libros/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@PostMapping("/libros/crear")
	public String crearLibroAdmin(@Valid Libro libro, BindingResult result, Model model,
			@RequestParam("foto_li") MultipartFile foto, SessionStatus status, RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			List<Local> locales = localService.findAll();
			model.addAttribute("locales", locales);
			return "/libros/crear";
		}

		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				libro.setFoto_libro(foto.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar la imagen del libro");
			}
		} else if (libro.getFoto_libro() == null || libro.getFoto_libro() == "") {
			libro.setFoto_libro("no-book.jpg");
		}

		try {
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre '" + libro.getTitulo() + "').");
			status.setComplete();
			return "redirect:/libros/listar";
		} catch (Exception e) {
			model.addAttribute("titulo", "Registro de Libro");
			model.addAttribute("libro", libro);
			List<Local> locales = localService.findAll();
			model.addAttribute("locales", locales);
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@GetMapping("/libros/editar/{id}")
	public String editarLibroAdmin(@PathVariable(value = "id") Long idlibro, Map<String, Object> modelMap,
			RedirectAttributes flash) {
		Libro libro = null;
		List<Local> locales = localService.findAll();
		modelMap.put("locales", locales);
		modelMap.put("editable", true);
		modelMap.put("titulo", "Modificar Libro");
		try {
			libro = libroService.findOne(idlibro);
			modelMap.put("libro", libro);
			return "/libros/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/libros/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@PostMapping("/libros/editar")
	public String guardarLibroAdmin(@Valid Libro libro, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("file") MultipartFile foto_libro) {
		if (result.hasErrors()) {
			List<Local> locales = localService.findAll();
			model.addAttribute("locales", locales);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Libro");
			return "/libros/editar";
		}

		if (!foto_libro.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
			byte[] bytes;
			try {
				bytes = foto_libro.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto_libro.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				libro.setFoto_libro(foto_libro.getOriginalFilename());
			} catch (IOException e) {
				model.addAttribute("error", e.getMessage());
			}

		} else if (libro.getFoto_libro() == null || libro.getFoto_libro() == "") {
			libro.setFoto_libro("no-book.jpg");
		}

		try {
			libroService.update(libro);
			flash.addFlashAttribute("warning",
					"El libro con código " + libro.getTitulo() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/libros/listar";
		} catch (Exception e) {
			model.addAttribute("libro", libro);
			List<Local> locales = localService.findAll();
			model.addAttribute("locales", locales);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Libro");
			return "/libros/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN')")
	@RequestMapping("/libros/deshabilitar/{id}")
	public String deshabilitarLibroAdmin(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Libro libro = null;
		try {
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.update(libro);
			flash.addFlashAttribute("info", "El libro '" + libro.getTitulo() + "' ha sido deshabilitado.");
			return "redirect:/libros/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/libros/listar";
		}
	}

	// ************************ ROLE ADMIN ************************
	// LISTADO DE LIBROS POR LOCAL Y USUARIO LOGUEADO (DESDE TABLA LOCAL)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping("/locales/libros/listar")
	public String listarLibrosPorLocalAdmin(Model model, Authentication authentication) {
		// BUSCA EL EMPLEADO LOGUEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.addAttribute("titulo", "Listado de Libros de '" + empleado.getLocal().getDireccion() + "'");
		// BUSCO LOS LIBROS POR SU LOCAL_ID Y POR EL ID_EMPLEADO
		List<Libro> libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(),
				empleado.getId());
		model.addAttribute("libros", libros);
		return "/libros/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/locales/libros/cancelar")
	public String cancelar() {
		return "redirect:/locales/libros/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/locales/libros/crear")
	public String crearLibro(Map<String, Object> model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		model.put("libro", new Libro());
		model.put("titulo", "Registro de Libro");
		model.put("local", empleado.getLocal());
		return "/libros/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping("/locales/libros/crear")
	public String crearLibro(@Valid Libro libro, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_li") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				return "/libros/crear";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/locales/libros/listar";
			}
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();

			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				libro.setFoto_libro(foto.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (libro.getFoto_libro() == null || libro.getFoto_libro() == "") {
			libro.setFoto_libro("no-book.jpg");
		}
		try {
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre '" + libro.getTitulo() + "').");
			status.setComplete();
			return "redirect:/locales/libros/listar";
		} catch (Exception e) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				return "/libros/crear";
			} catch (Exception e1) {
				model.addAttribute("error", e1.getMessage());
				return "redirect:/locales/libros/listar";
			}
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/locales/libros/editar/{id}")
	public String editarLibro(@PathVariable(value = "id") Long idlibro, Map<String, Object> modelMap,
			RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Libro libro = null;
		Local local;
		try {
			local = localService.findById(empleado.getLocal().getId());
			modelMap.put("editable", true);
			modelMap.put("titulo", "Modificar Libro");
			modelMap.put("local", local);
		} catch (Exception e1) {
			modelMap.put("error", e1.getMessage());
			return "redirect:/locales/libros/listar";
		}
		try {
			libro = libroService.findOne(idlibro);
			modelMap.put("libro", libro);
			return "/libros/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/libros/listar";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping("/locales/libros/editar")
	public String guardarLibro(@Valid Libro libro, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash, @RequestParam("foto_li") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				return "/libros/editar";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/locales/libros/listar";
			}
		}
		if (!foto.isEmpty()) {
			Path directorioRecursos = Paths.get("src//main//resources//static/uploads");
			String rootPath = directorioRecursos.toFile().getAbsolutePath();
			try {
				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				libro.setFoto_libro(foto.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (libro.getFoto_libro() == null || libro.getFoto_libro() == "") {
			libro.setFoto_libro("no-book.jpg");
		}
		try {
			libroService.update(libro);
			flash.addFlashAttribute("warning",
					"El libro con código " + libro.getTitulo() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/locales/libros/listar";
		} catch (Exception e) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				return "/libros/crear";
			} catch (Exception e1) {
				model.addAttribute("error", e1.getMessage());
				return "redirect:/locales/libros/listar";
			}
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping("/locales/libros/deshabilitar/{id}")
	public String deshabilitarLibro(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Libro libro = null;
		try {
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.update(libro);
			flash.addFlashAttribute("info", "El libro '" + libro.getTitulo() + "' ha sido deshabilitado.");
			return "redirect:/locales/libros/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/libros/listar";
		}
	}
}
