package com.biblioteca2020.controllers;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.biblioteca2020.models.entity.Categoria;
import com.biblioteca2020.models.service.ICategoriaService;

@Controller
@RequestMapping("/categorias")
@SessionAttributes("categoria")
public class CategoriaController {

	@Autowired
	private ICategoriaService categoriaService;

	// ############################# CRUD #############################
	// LISTADO DE CATEGORIAS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/listar")
	public String listarCategorias(Model model) {
		model.addAttribute("categorias", categoriaService.findAll());
		model.addAttribute("titulo", "Listado de categorias");
		return "categorias/listar";
	}

	// MÉTODO PARA REALIZAR LA BUSQUEDA DE CATEGORIAS POR SU NOMBRE MEDIANTE
	// AUTOCOMPLETADO
	@RequestMapping(value = "/cargar-categorias/{term}", produces = { "application/json" })
	public @ResponseBody List<Categoria> cargarCategorias(@PathVariable String term) {
		return categoriaService.findByNombreLikeIgnoreCase(term);
	}

	// REGRESAR A LISTADO DE CATEGORIAS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping("/cancelar")
	public String cancelar() {
		return "redirect:/categorias/listar";
	}

	// FORMULARIO DE REGISTRO DE CATEGORIAS
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/crear")
	public String crearCategoria(Model model) {
		model.addAttribute("titulo", "Registro de categoría nueva");
		model.addAttribute("categoria", new Categoria());
		return "categorias/crear";
	}

	// REGISTRAR CATEGORIA NUEVA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/crear")
	public String guardarCategoria(@Valid Categoria categoria, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		String ruta = "";
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Registro de categoría nueva");
			return "/categorias/crear";
		}
		try {
			categoria.setEstado(true);
			categoriaService.save(categoria);
			flash.addFlashAttribute("success",
					"Nueva categoría '" + categoria.getNombre() + "' registrada (Id: " + categoria.getId() + ")");
			status.setComplete();
			ruta = "redirect:/categorias/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			flash.addFlashAttribute("titulo", "Registro de categoría nueva");
			ruta = "redirect:/categorias/crear";
		}
		return ruta;
	}

	// FORMULARIO DE EDICIÓN DE CATEGORIA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@GetMapping(value = "/crear/{id}")
	public String editarCategorias(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		try {
			Categoria categoria = categoriaService.findOne(id);
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos de categoría");
			model.addAttribute("categoria", categoria);
			return "/categorias/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/categorias/listar";
		}
	}

	// ACTUALIZAR CATEGORIA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@PostMapping(value = "/editar")
	public String actualizarCategoria(@Valid Categoria categoria, BindingResult result, Model model,
			SessionStatus status, RedirectAttributes flash) {
		String ruta = "";
		if (result.hasErrors()) {
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar datos de categoría");
			return "/categorias/crear";
		}
		try {
			categoriaService.save(categoria);
			flash.addFlashAttribute("warning",
					"Categoría '" + categoria.getNombre() + "' (Id: " + categoria.getId() + ") actualizada");
			status.setComplete();
			ruta = "redirect:/categorias/listar";
		} catch (Exception e) {
			// SOSTITUYO EL MENSAJE DE ERROR GENÉRICO,
			// YA QUE ES POSIBLE SOLO UN TIPO DE ERROR EN ESTE MÈTODO: EL DE VIOLACIÒN DE
			// CONSTRAINT.
			flash.addFlashAttribute("error", e.getMessage().replace(e.getMessage(), "La categoría ya existe"));
			ruta = "redirect:/categorias/listar";
		}
		return ruta;
	}

	// DESHABILITAR CATEGORIA
	@PreAuthorize("hasAnyRole('ROLE_SYSADMIN', 'ROLE_ADMIN')")
	@RequestMapping(value = "/deshabilitar/{id}")
	public String deshabilitarCategoria(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		try {
			Categoria categoria = categoriaService.findOne(id);
			categoria.setEstado(false);
			categoriaService.save(categoria);
			flash.addFlashAttribute("info",
					"Categoría '" + categoria.getNombre() + "' (Id: " + categoria.getId() + ") deshabilitada");
			return "redirect:/categorias/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/categorias/listar";
		}
	}
}
