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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.biblioteca2020.models.entity.Categoria;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Local;
import com.biblioteca2020.models.service.ICategoriaService;
import com.biblioteca2020.models.service.IEmpleadoService;
import com.biblioteca2020.models.service.ILibroService;
import com.biblioteca2020.models.service.ILocalService;
import com.biblioteca2020.view.pdf.GenerarReportePDF;
import com.biblioteca2020.view.xlsx.GenerarReporteExcel;

@Controller
@SessionAttributes("libro")
public class LibroController {

	@Autowired
	private ILibroService libroService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private ILocalService localService;

	@Autowired
	private IEmpleadoService empleadoService;

	// LISTADO DE FORMATOS DE FOTO PERMITIDOS
	private static final List<String> formatosFoto = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	// ############################# CRUD #############################
	// LISTADO DE LIBROS POR LOCAL Y USUARIO LOGUEADO (DESDE TABLA LOCAL)
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = { "/locales/{id}/libros/listar", "/locales/libros/listar" })
	public String listarLibrosPorLocal(@PathVariable(value = "id") Optional<Long> idLocal, Model model,
			RedirectAttributes flash, Authentication authentication) {
		// BUSCA EL EMPLEADO LOGUEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros;
		Local local;
		String ruta = "";
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				try {
					// VALIDO SI ID LOCAL ES DE ALGÚN LOCAL EXISTENTE ..
					if (idLocal.isPresent()) {
						local = localService.fetchByIdWithEmpresa(idLocal.get());
						libros = libroService.findByLocal(local.getId());
						model.addAttribute("titulo", "Listado de libros de " + local.getDireccion());
						model.addAttribute("libros", libros);
						model.addAttribute("idLocal", idLocal.get());
						ruta = "/libros/listar";
						// .. SI NO, REDIRECCIONO AL HOME
					} else {
						flash.addFlashAttribute("error", "El local es inválido");
						ruta = "redirect:/home";
					}
				} catch (Exception e) {
					flash.addFlashAttribute("error", e.getMessage());
					ruta = "redirect:/home";
				}
				break;
			default:
				model.addAttribute("titulo", "Listado de libros de " + empleado.getLocal().getDireccion());
				// OBTENER LIBROS POR ID_LOCAL Y ID_EMPLEADO
				libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(), empleado.getId());
				model.addAttribute("libros", libros);
				ruta = "/libros/listar";
				break;
		}
		return ruta;
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA CATEGORIAS POR SU NOMBRE MEDIANTE
	// AUTOCOMPLETADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/libros/cargar-categorias/{term}", produces = { "application/json" })
	public @ResponseBody List<Categoria> cargarCategorias(@PathVariable String term) {
		return categoriaService.findByNombreLikeIgnoreCase(term);
	}

	// REGRESAR A LISTADO DE LIBROS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping({ "/locales/{idLocal}/libros/cancelar", "/locales/libros/cancelar" })
	public String cancelar(@PathVariable(value = "idLocal") Optional<Long> idLocal, Model model,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String ruta = "";
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				model.addAttribute("idLocal", idLocal.get());
				ruta = "redirect:/locales/" + idLocal.get() + "/libros/listar";
				break;
			default:
				ruta = "redirect:/locales/libros/listar";
				break;
		}
		return ruta;
	}

	// FORMULARIO DE REGISTRO DE LIBRO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping({ "/locales/{idLocal}/libros/crear", "/locales/libros/crear" })
	public String crearLibro(@PathVariable(value = "idLocal") Optional<Long> idLocal, Model model,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Libro libro = new Libro();
		List<Local> locales;
		libro.setFoto_libro("no-book.jpg");
		model.addAttribute("libro", libro);
		model.addAttribute("titulo", "Registro de libro nuevo");
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				locales = localService.findAll();
				model.addAttribute("locales", locales);
				model.addAttribute("idLocal", idLocal.get());
				break;
			default:
				model.addAttribute("local", empleado.getLocal());
				break;
		}
		return "/libros/crear";
	}

	// REGISTRAR LIBRO NUEVO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping({ "/locales/{idLocal}/libros/crear", "/locales/libros/crear" })
	public String crearLibro(@PathVariable(value = "idLocal") Optional<Long> idLocal, @Valid Libro libro,
			BindingResult result, Model model, SessionStatus status, RedirectAttributes flash,
			@RequestParam("foto_li") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Local> locales = null;
		String ruta = "";
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					locales = localService.findAll();
					model.addAttribute("idLocal", idLocal.get());
					ruta = "redirect:/locales/" + idLocal.get() + "/libros/listar";
					break;
				default:
					ruta = "redirect:/locales/libros/listar";
					break;
			}
			// CAPTURAR ERRORES DE FORMULARIO
			if (result.hasErrors()) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						model.addAttribute("locales", locales);
						model.addAttribute("idLocal", idLocal.get());
						break;
					default:
						model.addAttribute("local", empleado.getLocal());
						break;
				}
				model.addAttribute("titulo", "Registro de libro nuevo");
				model.addAttribute("libro", libro);
				return "/libros/crear";
			}
			// PREGUNTO SI EL PARAMETRO DE LA FOTO ES NULO ..
			if (!foto.isEmpty()) {
				// .. Y PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					libro.setFoto_libro(foto.getOriginalFilename());
				} else {
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_SYSADMIN]":
							model.addAttribute("locales", locales);
							model.addAttribute("idLocal", idLocal.get());
							break;
						default:
							model.addAttribute("local", empleado.getLocal());
							break;
					}
					model.addAttribute("titulo", "Registro de libro nuevo");
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("libro", libro);
					return "/libros/crear";
				}
			} else {
				libro.setFoto_libro("no-book.jpg");
			}
			// EXCEPCION DE FOTO
		} catch (IOException ioe) {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					model.addAttribute("locales", locales);
					model.addAttribute("idLocal", idLocal.get());
					break;
				default:
					model.addAttribute("local", empleado.getLocal());
					break;
			}
			model.addAttribute("titulo", "Registro de libro nuevo");
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("libro", libro);
			return "/libros/crear";
		}
		try {
			// GUARDAR REGISTRO
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre " + libro.getTitulo() + ")");
			status.setComplete();
			// EXCEPCION GENERAL
		} catch (Exception ex) {
			flash.addFlashAttribute("error", ex.getMessage());
		}
		return ruta;
	}

	// FORMULARIO DE EDICION DE LIBRO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping({ "/locales/{idLocal}/libros/editar/{id}", "/locales/libros/editar/{id}" })
	public String editarLibro(@PathVariable(value = "idLocal") Optional<Long> idLocal,
			@PathVariable(value = "id") Long idlibro, Model model, RedirectAttributes flash,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Local> locales = null;
		String ruta = "";
		try {
			Libro libro = libroService.findOne(idlibro);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos de libro");
			model.addAttribute("libro", libro);
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					locales = localService.findAll();
					model.addAttribute("locales", locales);
					model.addAttribute("idLocal", idLocal.get());
					break;
				default:
					model.addAttribute("local", empleado.getLocal());
					break;
			}
			ruta = "/libros/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					ruta = "redirect:/locales/" + idLocal.get() + "/libros/listar";
					break;
				default:
					ruta = "redirect:/locales/libros/listar";
					break;
			}
		}
		return ruta;
	}

	// ACTUALIZAR LIBRO - BUG CON @VALID
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping({ "/locales/{idLocal}/libros/editar", "/locales/libros/editar" })
	public String guardarLibro(@PathVariable(value = "idLocal") Optional<Long> idLocal, Libro libro,
			BindingResult result, Model model, SessionStatus status, RedirectAttributes flash,
			@RequestParam("foto_li") MultipartFile foto, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Local> locales = null;
		String ruta = "";
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					locales = localService.findAll();
					model.addAttribute("idLocal", idLocal.get());
					ruta = "redirect:/locales/" + idLocal.get() + "/libros/listar";
					break;
				default:
					ruta = "redirect:/locales/libros/listar";
					break;
			}
			// CAPTURAR ERRORES DE FORMULARIO
			if (result.hasErrors()) {
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						model.addAttribute("locales", locales);
						model.addAttribute("idLocal", idLocal.get());
						break;
					default:
						model.addAttribute("local", empleado.getLocal());
						break;
				}
				model.addAttribute("titulo", "Modificar datos de libro");
				model.addAttribute("libro", libro);
				return "/libros/crear";
			}
			// PREGUNTO SI EL PARAMETRO DE LA FOTO ES NULO ..
			if (!foto.isEmpty()) {
				// .. Y PREGUNTO SI MI FILE TIENE EL FORMATO DE IMAGEN
				if (formatosFoto.contains(foto.getContentType())) {
					String rootPath = "C://Temp//uploads";
					byte[] bytes = foto.getBytes();
					Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
					Files.write(rutaCompleta, bytes);
					libro.setFoto_libro(foto.getOriginalFilename());
				} else {
					switch (userDetails.getAuthorities().toString()) {
						case "[ROLE_SYSADMIN]":
							model.addAttribute("locales", locales);
							model.addAttribute("idLocal", idLocal.get());
							break;
						default:
							model.addAttribute("local", empleado.getLocal());
							break;
					}
					model.addAttribute("titulo", "Modificar datos de libro");
					model.addAttribute("error", "El formato de la foto es incorrecto");
					model.addAttribute("libro", libro);
					return "/libros/crear";
				}
			}
			// EXCEPCION DE FOTO
		} catch (IOException ioe) {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					model.addAttribute("locales", locales);
					model.addAttribute("idLocal", idLocal.get());
					break;
				default:
					model.addAttribute("local", empleado.getLocal());
					break;
			}
			model.addAttribute("titulo", "Modificar datos de libro");
			model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
			model.addAttribute("libro", libro);
			return "/libros/crear";
		}
		try {
			// ACTUALIZAR REGISTRO
			libroService.update(libro);
			flash.addFlashAttribute("warning",
					"El libro " + libro.getTitulo() + " ha sido actualizado en la base de datos");
			status.setComplete();
		} catch (Exception ex) {
			flash.addFlashAttribute("error", ex.getMessage());
		}
		return ruta;
	}

	// DESHABILITAR LIBRO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping({ "/locales/{idLocal}/libros/deshabilitar/{id}", "/locales/libros/deshabilitar/{id}" })
	public String deshabilitarLibro(@PathVariable(value = "idLocal") Optional<Long> idLocal,
			@PathVariable(value = "id") Long id, Authentication authentication, RedirectAttributes flash) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Libro libro;
		String ruta = "";
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					flash.addFlashAttribute("idLocal", idLocal.get());
					ruta = "redirect:/locales/" + idLocal.get() + "/libros/listar";
					break;
				default:
					ruta = "redirect:/locales/libros/listar";
					break;
			}
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.update(libro);
			flash.addFlashAttribute("info", "El libro " + libro.getTitulo() + " ha sido deshabilitado");
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
		}
		return ruta;
	}

	// ############################# REPORTES #############################
	// FORMULARIO DE REPORTES LIBROS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = { "/locales/{id}/libros/reportes", "/locales/libros/reportes" })
	public String crearReporte(@PathVariable(value = "id") Optional<Long> idLocal, Model model,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		model.addAttribute("titulo", "Creación de Reportes");
		model.addAttribute("libro", new Libro());
		ArrayList<Boolean> estados = new ArrayList<Boolean>();
		estados.add(true);
		estados.add(false);
		model.addAttribute("estados", estados);
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				model.addAttribute("idLocal", idLocal.get());
				break;
			default:
				break;
		}
		return "/libros/crear_reporte";
	}

	// GENERAR REPORTE PDF LIBROS UNICOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/{idLocal}/libros/reportes/pdf/libros-unicos", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosUnicos(
			@PathVariable(value = "idLocal") Optional<Long> idLocal, Model model, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					libros = libroService.fetchWithCategoriaWithLocal();
					model.addAttribute("role", userDetails.getAuthorities().toString());
					break;
				default:
					libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(),
							empleado.getId());
					break;
			}
			if (libros.size() != 0) {
				bis = GenerarReportePDF.generarPDFLibros(model, "Reporte de Libros Únicos", libros);
				headers.add("Content-Disposition", "inline; filename=listado-libros-unicos.pdf");
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

	// BUSCAR LIBROS POR CATEGORIA PARA GENERAR REPORTE
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/{idLocal}/libros/reportes/buscar-libros-por-categoria", method = RequestMethod.GET)
	public String buscarCategoriaParaReporte(@PathVariable(value = "idLocal") Optional<Long> idLocal,
			@RequestParam String categoria_libro, Model model, Authentication authentication,
			RedirectAttributes flash) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		model.addAttribute("titulo", "Reporte Por Categoría");
		model.addAttribute("enable", true);
		Categoria categoria;
		String ruta = "";
		try {
			if (categoria_libro.length() == 0 || categoria_libro.length() > 100) {
				flash.addFlashAttribute("error", "Error, la categoría es incorrecta");
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						model.addAttribute("idLocal", idLocal.get());
						ruta = "redirect:/locales/" + idLocal.get() + "/libros/reportes";
						break;
					default:
						ruta = "redirect:/locales/libros/reportes";
						break;
				}
			}
			categoria = categoriaService.findByNombre(categoria_libro);
			if (categoria == null) {
				flash.addFlashAttribute("error", "Error, la categoría no está disponible");
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						model.addAttribute("idLocal", idLocal.get());
						ruta = "redirect:/locales/" + idLocal.get() + "/libros/reportes";
						break;
					default:
						ruta = "redirect:/locales/libros/reportes";
						break;
				}
			}
			model.addAttribute("categoria", categoria);
			ruta = "/libros/busqueda_libros_por_categoria";
		} catch (Exception e) {
			flash.addFlashAttribute("error", "Error, la categoría es incorrecta o no está disponible");
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					model.addAttribute("idLocal", idLocal.get());
					ruta = "redirect:/locales/" + idLocal.get() + "/libros/reportes";
					break;
				default:
					ruta = "redirect:/locales/libros/reportes";
					break;
			}
		}
		return ruta;
	}

	// GENERAR REPORTE PDF LIBROS POR CATEGORÍA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/{idLocal}/libros/reportes/pdf/libros-por-categoria/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosPorCategoria(
			@PathVariable(value = "id") Optional<Long> idLocal, @PathVariable("idLocal") String id,
			Authentication authentication, Model model) {
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
			Categoria categoria = categoriaService.findOne(Long.parseLong(id));
			List<Libro> libros = null;

			libros = libroService.findByCategoriaAndLocal(categoria.getNombre(), empleado.getLocal().getId());
			if (libros.size() != 0) {
				bis = GenerarReportePDF.generarPDFLibros(model, "Reporte de Libros Por Categoría", libros);
				headers.add("Content-Disposition", "inline; filename=listado-libros-por-categoria.pdf");
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

	// GENERAR REPORTE PDF LIBROS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/{idLocal}/libros/reportes/pdf/libros-por-estado/{estado}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosPorEstado(
			@PathVariable(value = "idLocal") Optional<Long> idLocal, @PathVariable("estado") String estado, Model model,
			Authentication authentication) {
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		try {
			List<Libro> libros = null;
			String titulo = "";
			String tituloPdf = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				libros = libroService.findByLocalAndEstado(empleado.getLocal().getId(), true);
				titulo = "listado-libros-disponibles";
				tituloPdf = "Reporte de Libros Disponibles";
			} else if (estado.equals("false")) {
				libros = libroService.findByLocalAndEstado(empleado.getLocal().getId(), false);
				titulo = "listado-libros-no-disponibles";
				tituloPdf = "Reporte de Libros No Disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (libros.size() != 0) {
				bis = GenerarReportePDF.generarPDFLibros(model, tituloPdf, libros);
				headers.add("Content-Disposition", "inline; filename=" + titulo + ".pdf");
				return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
						.body(new InputStreamResource(bis));
			} else {
				headers.clear();
				headers.add("Location", "/error_reporte");
				headers.set("errorMessage", "Error, el reporte solicitado no existe.");
				model.addAttribute("errorMessage", "Error, el reporte solicitado no existe.");
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

	// GENERAR REPORTE EXCEL LIBROS UNICOS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/{idLocal}/libros/reportes/xlsx/libros-unicos", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> generarExcelLibrosUnicos(
			@PathVariable(value = "idLocal") Optional<Long> idLocal, Authentication authentication, Model model) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					libros = libroService.fetchWithCategoriaWithLocal();
					model.addAttribute("role", userDetails.getAuthorities().toString());
					break;
				default:
					libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(),
							empleado.getId());
					break;
			}
			if (libros.size() != 0) {
				bis = GenerarReporteExcel.generarExcelLibros(model, "Reporte de Libros Unicos", libros);
				headers.add("Content-Disposition", "attachment; filename=listado-libros-unicos.xlsx");
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

	// GENERAR REPORTE EXCEL LIBROS POR CATEGORÍA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/xlsx/libros-por-categoria/{id}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repLibrosPorCategoria(@PathVariable("id") String id,
			Authentication authentication, Model model) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		ByteArrayInputStream in;
		var headers = new HttpHeaders();
		try {
			Categoria categoria = categoriaService.findOne(Long.parseLong(id));
			libros = libroService.findByCategoriaAndLocal(categoria.getNombre(), empleado.getLocal().getId());
			if (libros.size() != 0) {
				in = GenerarReporteExcel.generarExcelLibros(model, "Reporte de Libros Por Categoría", libros);
				headers.add("Content-Disposition", "attachment; filename=listado-libros-por-categoria.xlsx");
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

	// GENERAR REPORTE EXCEL LIBROS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/xlsx/libros-por-estado/{estado}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repLibrosPorEstado(@PathVariable("estado") String estado,
			Authentication authentication, Model model) {
		List<Libro> libros = null;
		ByteArrayInputStream in;
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		var headers = new HttpHeaders();
		try {
			String titulo = "";
			String tituloExcel = "";
			// USO UN STRING EN VEZ DE UN BOOLEAN PARA HACER SALTAR LA EXCEPCION
			if (estado.equals("true")) {
				libros = libroService.findByLocalAndEstado(empleado.getLocal().getId(), true);
				titulo = "listado-libros-disponibles";
				tituloExcel = "Reporte de Libros Disponibles";
			} else if (estado.equals("false")) {
				libros = libroService.findByLocalAndEstado(empleado.getLocal().getId(), false);
				titulo = "listado-libros-no-disponibles";
				tituloExcel = "Reporte de Libros No Disponibles";
			} else if (!estado.equals("true") || estado.equals("false")) {
				headers.clear();
				headers.add("Location", "/error_reporte");
				return new ResponseEntity<InputStreamResource>(null, headers, HttpStatus.FOUND);
			}
			if (libros.size() != 0) {
				in = GenerarReporteExcel.generarExcelLibros(model, tituloExcel, libros);
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
}
