package com.biblioteca2020.controllers;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.biblioteca2020.models.entity.Categoria;
import com.biblioteca2020.models.service.ICategoriaService;

@Controller
@SessionAttributes("categoria")
public class CategoriaController {

	@Autowired
	private ICategoriaService categoriaService;
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_EMPLEADO')")
	@RequestMapping(value = "/categorias/listar", method = RequestMethod.GET)
	public String listarCategorias(Model model) {
		model.addAttribute("titulo", "Listado de Categorias");
		model.addAttribute("categorias", categoriaService.findByEstado(true));
		return "/categorias/listar";
	}
	
	@RequestMapping(value = "/categoria/cargarCategorias/{term}", produces = { "application/json" })
	public @ResponseBody List<Categoria> cargarCategorias(@PathVariable String term) {
		return categoriaService.findByNombreLikeIgnoreCase(term);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@RequestMapping(value = "/categorias/crear")
	public String crearCategoria(Map<String, Object> model) {
		Categoria categoria = new Categoria();
		model.put("titulo", "Formulario de Categoria");
		model.put("categoria", categoria);
		return "/categorias/crear";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@RequestMapping(value = "/categorias/crear", method = RequestMethod.POST)
	public String guardarCategoria(@Valid Categoria categoria, BindingResult result, Model model, SessionStatus status,
			RedirectAttributes flash) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Categoria");
			return "/categorias/crear";
		}
		categoria.setEstado(true);
		categoriaService.save(categoria);
		flash.addFlashAttribute("success",
				"La categoría ha sido registrada en la base de datos (Código " + categoria.getId() + ").");
		status.setComplete();
		return "redirect:/categorias/listar";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
	@RequestMapping(value = "/categorias/crear/{id}")
	public String editarCategorias(@PathVariable(value = "id") Long id, Map<String, Object> model,
			RedirectAttributes flash) {
		Categoria categoria = null;
		if (id > 0) {
			categoria = categoriaService.findOne(id);
		} else {
			return "redirect:/categorias/listar";
		}
		model.put("titulo", "Editar Categoria");
		model.put("categoria", categoria);
		flash.addFlashAttribute("success",
				"Los datos de la categoría con código " + categoria.getId() + " han sido actualizados.");
		return "/categorias/crear";
	}
	
	/*@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/categorias/eliminar/{id}")
	public String eliminarCategoria(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			Categoria categoria = categoriaService.findOne(id);
			flash.addFlashAttribute("success",
					"La categoría con código " + categoria.getId() + " ha sido eliminada.");
			// EN REALIDAD TENGO QUE DESHABILITAR EL REGISTRO EN LA TABLA CATEGORÍA
			// Y ELIMINAR EL REGISTRO EN LA TABLA libros_locales (ELIMINO EL LIBRO CON ESA CATEGORIA)
			categoriaService.delete(id);
		}
		return "redirect:/categorias/listar";
	}*/

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/categorias/deshabilitar/{id}")
	public String deshabilitarCategoria(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Categoria categoria = null;
		if (id > 0) {
			categoria = categoriaService.findOne(id);
			categoria.setEstado(false);
			categoriaService.save(categoria);
			flash.addFlashAttribute("warning",
					"La categoría con el código " + categoria.getId() + " ha sido deshabilitada.");
		}
		return "redirect:/categorias/listar";
	}
}
