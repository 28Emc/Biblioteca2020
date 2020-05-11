package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Prestamo;

public interface IPrestamoService {
	// USADO
	public Prestamo findById(Long id);

	// USADO
	public List<Prestamo> findAll();

	// USADO
	public List<Prestamo> fetchWithLibroWithUsuarioWithEmpleado();

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleado(Long id_local);

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(Long idEmpleado);

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerLibro(Long idLibro);

	// USADO
	public List<Prestamo> fetchWithLibroWithUsuarioWithEmpleado(Long idLibro);

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(Long id);

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserAll(Long id);

	// USADO
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(Long id);

	// USADO
	public void save(Prestamo prestamo);

	// USADO
	public void delete(Long id);

	// USADO
	public String mostrarFechaAmigable();
}
