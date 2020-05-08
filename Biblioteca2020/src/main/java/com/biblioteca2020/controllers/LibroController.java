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

	private static final List<String> formatosFoto = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	// ************************ ROLE ADMIN ************************
	// LISTADO DE LIBROS POR LOCAL Y USUARIO LOGUEADO (DESDE TABLA LOCAL)
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/locales/libros/listar/{id}")
	public String listarLibrosPorLocalAdmin(@PathVariable(value = "id") Long idLocal, Model model,
			Authentication authentication) {
		// BUSCA EL EMPLEADO LOGUEADO
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros;
		Local local;
		String ruta = "";
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				try {
					local = localService.fetchByIdWithEmpresa(idLocal);
					libros = libroService.findByLocal(local.getId());
					model.addAttribute("titulo", "Listado de libros de " + local.getDireccion());
					model.addAttribute("libros", libros);
					model.addAttribute("idLocal", idLocal);
					ruta = "/libros/listar";
				} catch (Exception e) {
					model.addAttribute("error", e.getMessage());
					ruta = "/home";
				}
				break;
			default:
				model.addAttribute("titulo", "Listado de libros de " + empleado.getLocal().getDireccion());
				// BUSCO LOS LIBROS POR SU LOCAL_ID Y POR EL ID_EMPLEADO
				libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(), empleado.getId());
				model.addAttribute("libros", libros);
				model.addAttribute("idLocal", idLocal);
				ruta = "/libros/listar";
				break;
		}
		return ruta;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/locales/libros/reportes/{id}")
	public String crearReporte(@PathVariable(value = "id") Long idLocal, Model model, Authentication authentication) {
		model.addAttribute("titulo", "Creación de Reportes");
		model.addAttribute("libro", new Libro());
		ArrayList<Boolean> estados = new ArrayList<Boolean>();
		estados.add(true);
		estados.add(false);
		model.addAttribute("estados", estados);
		model.addAttribute("idLocal", idLocal);
		return "/libros/crear_reporte";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA CATEGORIAS POR SU NOMBRE MEDIANTE
	// AUTOCOMPLETADO
	@RequestMapping(value = "/libros/cargar-categorias/{term}", produces = { "application/json" })
	public @ResponseBody List<Categoria> cargarCategorias(@PathVariable String term) {
		return categoriaService.findByNombreLikeIgnoreCase(term);
	}

	// REPORTE PDF LIBROS UNICOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/pdf/libros-unicos", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosUnicos(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(), empleado.getId());
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (libros.size() != 0) {
				bis = GenerarReportePDF.generarPDFLibros("Reporte de Libros Únicos", libros);
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/buscar-libros-por-categoria", method = RequestMethod.GET)
	public String buscarCategoriaParaReporte(@RequestParam String categoria_libro, Model model,
			Authentication authentication, RedirectAttributes flash) {
		model.addAttribute("titulo", "Reporte Por Categoría");
		model.addAttribute("enable", true);
		Categoria categoria;
		try {
			if (categoria_libro.length() == 0 || categoria_libro.length() > 100) {
				flash.addFlashAttribute("error", "Error, la categoría es incorrecta");
				return "redirect:/locales/libros/reportes";
			}
			categoria = categoriaService.findByNombre(categoria_libro);
			if (categoria == null) {
				flash.addFlashAttribute("error", "Error, la categoría no está disponible");
				return "redirect:/locales/libros/reportes";
			}
			model.addAttribute("categoria", categoria);
		} catch (Exception e) {
			flash.addFlashAttribute("error", "Error, la categoría es incorrecta o no está disponible");
			return "redirect:/locales/libros/reportes";
		}
		return "/libros/busqueda_libros_por_categoria";
	}

	// REPORTE PDF LIBROS POR CATEGORÍA
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/pdf/libros-por-categoria/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosPorCategoria(@PathVariable("id") String id,
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
				bis = GenerarReportePDF.generarPDFLibros("Reporte de Libros Por Categoría", libros);
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

	// REPORTE PDF LIBROS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/pdf/libros-por-estado/{estado}", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> generarPdfLibrosPorEstado(@PathVariable("estado") String estado,
			Model model, Authentication authentication) {
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
				bis = GenerarReportePDF.generarPDFLibros(tituloPdf, libros);
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

	// REPORTE EXCEL LIBROS UNICOS
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/xlsx/libros-unicos", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> generarExcelLibrosUnicos(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		libros = libroService.fetchByIdWithLocalesWithEmpleado(empleado.getLocal().getId(), empleado.getId());
		ByteArrayInputStream bis;
		var headers = new HttpHeaders();
		try {
			if (libros.size() != 0) {
				bis = GenerarReporteExcel.generarExcelLibros("Reporte de Libros Unicos", libros);
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

	// REPORTE EXCEL LIBROS POR CATEGORÍA
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/xlsx/libros-por-categoria/{id}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repLibrosPorCategoria(@PathVariable("id") String id,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		List<Libro> libros = null;
		ByteArrayInputStream in;
		var headers = new HttpHeaders();
		try {
			Categoria categoria = categoriaService.findOne(Long.parseLong(id));
			libros = libroService.findByCategoriaAndLocal(categoria.getNombre(), empleado.getLocal().getId());
			if (libros.size() != 0) {
				in = GenerarReporteExcel.generarExcelLibros("Reporte de Libros Por Categoría", libros);
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

	// REPORTE EXCEL LIBROS POR ESTADO
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/locales/libros/reportes/xlsx/libros-por-estado/{estado}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> repLibrosPorEstado(@PathVariable("estado") String estado,
			Authentication authentication) {
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
				in = GenerarReporteExcel.generarExcelLibros(tituloExcel, libros);
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

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/locales/libros/cancelar/{idLocal}")
	public String cancelar(@PathVariable(value = "idLocal") Long idLocal, Model model) {
		model.addAttribute("idLocal", idLocal);
		return "redirect:/locales/libros/listar/" + idLocal;
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/locales/libros/crear/{idLocal}")
	public String crearLibro(@PathVariable(value = "idLocal") Long idLocal, Model model,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		Libro libro = new Libro();
		libro.setFoto_libro("no-book.jpg");
		model.addAttribute("libro", libro);
		model.addAttribute("idLocal", idLocal);
		model.addAttribute("titulo", "Registro de libro nuevo");
		switch (userDetails.getAuthorities().toString()) {
			case "[ROLE_SYSADMIN]":
				List<Local> locales = localService.findAll();
				model.addAttribute("locales", locales);
				break;
			default:
				model.addAttribute("local", empleado.getLocal());
				break;
		}
		return "/libros/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping("/locales/libros/crear/{idLocal}")
	public String crearLibro(@PathVariable(value = "idLocal") Long idLocal, @Valid Libro libro, BindingResult result,
			Model model, SessionStatus status, RedirectAttributes flash, @RequestParam("foto_li") MultipartFile foto,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			try {
				Local local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				model.addAttribute("idLocal", idLocal);
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						List<Local> locales = localService.findAll();
						model.addAttribute("locales", locales);
						break;
					default:
						model.addAttribute("local", empleado.getLocal());
						break;
				}
				return "/libros/crear";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/locales/libros/listar/" + idLocal;
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
					libro.setFoto_libro(foto.getOriginalFilename());
				} catch (IOException e) {
					model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
					try {
						Local local = localService.findById(empleado.getLocal().getId());
						model.addAttribute("titulo", "Registro de Libro");
						model.addAttribute("libro", libro);
						model.addAttribute("local", local);
						model.addAttribute("idLocal", idLocal);
						return "/libros/crear";
					} catch (Exception e2) {
						model.addAttribute("error", e2.getMessage());
						return "redirect:/locales/libros/listar/" + idLocal;
					}
				}
			} else {
				model.addAttribute("error", "El formato de la foto es incorrecto");
				try {
					Local local = localService.findById(empleado.getLocal().getId());
					model.addAttribute("titulo", "Registro de Libro");
					model.addAttribute("libro", libro);
					model.addAttribute("local", local);
					model.addAttribute("idLocal", idLocal);
					return "/libros/crear";
				} catch (Exception e3) {
					model.addAttribute("error", e3.getMessage());
					return "redirect:/locales/libros/listar/" + idLocal;
				}
			}
		} else {
			libro.setFoto_libro("no-book.jpg");
		}
		try {
			libroService.save(libro);
			flash.addFlashAttribute("success",
					"El libro ha sido registrado en la base datos (Nombre '" + libro.getTitulo() + "').");
			status.setComplete();
			return "redirect:/locales/libros/listar/" + idLocal;
		} catch (Exception e) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				model.addAttribute("idLocal", idLocal);
				switch (userDetails.getAuthorities().toString()) {
					case "[ROLE_SYSADMIN]":
						List<Local> locales = localService.findAll();
						model.addAttribute("locales", locales);
						break;
					default:
						model.addAttribute("local", empleado.getLocal());
						break;
				}
				return "/libros/crear";
			} catch (Exception e1) {
				model.addAttribute("error", e1.getMessage());
				return "redirect:/locales/libros/listar/" + idLocal;
			}
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/locales/libros/editar/{idLocal}/{id}")
	public String editarLibro(@PathVariable(value = "idLocal") Long idLocal, @PathVariable(value = "id") Long idlibro,
			Model model, RedirectAttributes flash, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		try {
			Local local = localService.findById(empleado.getLocal().getId());
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Libro");
			model.addAttribute("local", local);
			model.addAttribute("idLocal", idLocal);
			switch (userDetails.getAuthorities().toString()) {
				case "[ROLE_SYSADMIN]":
					List<Local> locales = localService.findAll();
					model.addAttribute("locales", locales);
					break;
				default:
					model.addAttribute("local", empleado.getLocal());
					break;
			}
			Libro libro = libroService.findOne(idlibro);
			model.addAttribute("libro", libro);
			return "/libros/crear";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "redirect:/locales/libros/listar/" + idLocal;
		}
	}

	// BUG CON @VALID
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping("/locales/libros/editar/{idLocal}")
	public String guardarLibro(@PathVariable(value = "idLocal") Long idLocal, Libro libro, BindingResult result,
			Model model, SessionStatus status, RedirectAttributes flash, @RequestParam("foto_li") MultipartFile foto,
			Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
		if (result.hasErrors()) {
			try {
				Local local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				model.addAttribute("idLocal", idLocal);
				return "/libros/crear";
			} catch (Exception e) {
				flash.addFlashAttribute("error", e.getMessage());
				return "redirect:/locales/libros/editar/" + idLocal;
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
					libro.setFoto_libro(foto.getOriginalFilename());
				} catch (IOException e) {
					model.addAttribute("error", "Lo sentimos, hubo un error a la hora de cargar tu foto");
					try {
						Local local = localService.findById(empleado.getLocal().getId());
						model.addAttribute("titulo", "Registro de Libro");
						model.addAttribute("libro", libro);
						model.addAttribute("local", local);
						model.addAttribute("idLocal", idLocal);
						return "/libros/crear";
					} catch (Exception e2) {
						flash.addFlashAttribute("error", e2.getMessage());
						return "redirect:/locales/libros/editar/" + idLocal;
					}
				}
			} else {
				model.addAttribute("error", "El formato de la foto es incorrecto");
				try {
					Local local = localService.findById(empleado.getLocal().getId());
					model.addAttribute("titulo", "Registro de Libro");
					model.addAttribute("libro", libro);
					model.addAttribute("local", local);
					model.addAttribute("idLocal", idLocal);
					return "/libros/crear";
				} catch (Exception e3) {
					flash.addFlashAttribute("error", e3.getMessage());
					return "redirect:/locales/libros/editar/" + idLocal;
				}
			}
		}
		try {
			libroService.update(libro);
			flash.addFlashAttribute("warning",
					"El libro con código " + libro.getTitulo() + " ha sido actualizado en la base de datos.");
			status.setComplete();
			return "redirect:/locales/libros/listar/" + idLocal;
		} catch (Exception e) {
			Local local;
			try {
				local = localService.findById(empleado.getLocal().getId());
				model.addAttribute("titulo", "Registro de Libro");
				model.addAttribute("libro", libro);
				model.addAttribute("local", local);
				model.addAttribute("idLocal", idLocal);
				return "/libros/crear";
			} catch (Exception e1) {
				flash.addFlashAttribute("error", e1.getMessage());
				return "redirect:/locales/libros/listar/" + idLocal;
			}
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping("/locales/libros/deshabilitar/{idLocal}/{id}")
	public String deshabilitarLibro(@PathVariable(value = "idLocal") Long idLocal, @PathVariable(value = "id") Long id,
			RedirectAttributes flash) {
		Libro libro = null;
		try {
			libro = libroService.findOne(id);
			libro.setEstado(false);
			libroService.update(libro);
			flash.addFlashAttribute("idLocal", idLocal);
			flash.addFlashAttribute("info", "El libro '" + libro.getTitulo() + "' ha sido deshabilitado.");
			return "redirect:/locales/libros/listar/" + idLocal;
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/locales/libros/listar/" + idLocal;
		}
	}
}
