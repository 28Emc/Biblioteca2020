package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Prestamo;

public interface IPrestamoDao extends CrudRepository<Prestamo, Long> {

	// TODOS LOS PRESTAMOS CON LIBROS, USUARIOS Y EMPLEADOS FILTRADOS POR LOCAL DEL
	// ADMIN
	// USADO
	@Query("select p from Prestamo p join fetch p.usuario pe join fetch p.libro li join fetch p.empleado em where em.local.id = ?1")
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleado(Long id_local);

	// PRESTAMOS CON LIBROS, USUARIOS Y EMPLEADOS FILTRADOS POR ID_EMPLEADO
	// USADO
	@Query("select p from Prestamo p join fetch p.usuario pe join fetch p.libro li join fetch p.empleado em where em.id like ?1 or em.id = 1")
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(Long idEmpleado);

	// PRESTAMOS CON LIBROS, USUARIOS Y EMPLEADOS FILTRADOS POR ID_USUARIO
	// (HISTORIAL)
	// USADO
	@Query("select p from Prestamo p join fetch p.usuario pu join fetch p.libro pli join fetch p.empleado pem where pu.id like ?1 and p.devolucion = 1")
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(Long id);

	// USADO
	@Query("select p from Prestamo p join fetch p.usuario pu join fetch p.libro pli join fetch p.empleado pem where pu.id like ?1")
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserAll(Long id);

	// PRESTAMOS PENDIENTES POR ID_USUARIO (HISTORIAL)
	// USADO
	@Query("select p from Prestamo p join fetch p.usuario pu join fetch p.libro pli join fetch p.empleado pem where pu.id like ?1 and pem.username like '%Prueba%' and p.devolucion = 0")
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUserPendientes(Long id);

}
