package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;

public interface IPrestamoService {
	// USADO
	public Prestamo findById(Long id);
	
	public List<Prestamo> findAll();
	
	public List<Libro> findByTitulo(String term);
		
	public List<Prestamo> findByDevolucion(String devolucion);
	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleado();
	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(Long idEmpleado);
	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(Long id);

	public void save(Prestamo prestamo);
		
	public void update(Prestamo prestamo);
	// USADO
	public String mostrarFechaAmigable();	
}
