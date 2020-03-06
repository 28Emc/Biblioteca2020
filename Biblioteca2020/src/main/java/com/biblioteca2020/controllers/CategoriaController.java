package com.biblioteca2020.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@GetMapping(value = "/listar")
	public String listarCategorias(Model model) {
		model.addAttribute("categoria", new Categoria());
		model.addAttribute("categorias", categoriaService.findAll());
		model.addAttribute("titulo", "Listado de Categorias");
		return "categorias/listar";
	}

	@RequestMapping(value = "/cargarCategorias/{term}", produces = { "application/json" })
	public @ResponseBody List<Categoria> cargarCategorias(@PathVariable String term) {
		return categoriaService.findByNombreLikeIgnoreCase(term);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping("/cancelar")
	public String cancelar(ModelMap modelMap) {
		return "redirect:/categorias/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@RequestMapping(value = "/crear")
	public String crearFormCategoria(Map<String, Object> modelMap, Principal principal, RedirectAttributes flash) {
		Categoria categoria = new Categoria();
		modelMap.put("titulo", "Registro de Categoría");
		modelMap.put("categoria", categoria);
		return "categorias/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/crear")
	public String crearCategoria(@Valid Categoria categoria, BindingResult result, Model model,
			Map<String, Object> modelMap, SessionStatus status, RedirectAttributes flash, Principal principal) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Registro de Categoría");
			return "categorias/crear";
		}
		try {
			categoria.setEstado(true);
			categoriaService.save(categoria);
			flash.addFlashAttribute("success",
					"La categoría ha sido registrada en la base de datos (Código " + categoria.getId() + ").");
			status.setComplete();
			return "redirect:/categorias/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "/categorias/crear";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@GetMapping(value = "/crear/{id}")
	public String editarCategorias(@PathVariable(value = "id") Long id, Map<String, Object> model,
			RedirectAttributes flash) {
		Categoria categoria = null;
		try {
			categoria = categoriaService.findOne(id);
			model.put("editable", true);
			model.put("titulo", "Modificar Categoría");
			model.put("categoria", categoria);
			return "/categorias/crear";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/categorias/listar";
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@PostMapping(value = "/editar")
	public String guardarCategoria(@Valid Categoria categoria, BindingResult result, Model model,
			Map<String, Object> modelMap, SessionStatus status, RedirectAttributes flash, Principal principal) {
		if (result.hasErrors()) {
			model.addAttribute("editable", true);
			model.addAttribute("titulo", "Modificar Categoría");
			return "categorias/crear";
		}
		categoriaService.update(categoria);
		flash.addFlashAttribute("info", "La categoría '" + categoria.getNombre() + " (" + categoria.getId()
				+ ")' ha sido actualizada en la base de datos.");
		status.setComplete();
		return "redirect:/categorias/listar";
	}

	/*
	 * @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	 * 
	 * @RequestMapping(value = "/categorias/eliminar/{id}") public String
	 * eliminarCategoria(@PathVariable(value = "id") Long id, RedirectAttributes
	 * flash) { if (id > 0) { Categoria categoria = categoriaService.findOne(id);
	 * flash.addFlashAttribute("success", "La categoría con código " +
	 * categoria.getId() + " ha sido eliminada."); // EN REALIDAD TENGO QUE
	 * DESHABILITAR EL REGISTRO EN LA TABLA CATEGORÍA // Y ELIMINAR EL REGISTRO EN
	 * LA TABLA libros_locales (ELIMINO EL LIBRO CON ESA CATEGORIA)
	 * categoriaService.delete(id); } return "redirect:/categorias/listar"; }
	 */

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/deshabilitar/{id}")
	public String deshabilitarCategoria(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Categoria categoria = null;
		try {
			categoria = categoriaService.findOne(id);
			categoria.setEstado(false);
			categoriaService.update(categoria);
			flash.addFlashAttribute("warning",
					"La categoría con el código " + categoria.getId() + " ha sido deshabilitada.");
			return "redirect:/categorias/listar";
		} catch (Exception e) {
			flash.addFlashAttribute("error", e.getMessage());
			return "redirect:/categorias/listar";
		}
	}
}
