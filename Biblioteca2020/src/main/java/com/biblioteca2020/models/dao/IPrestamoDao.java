package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Prestamo;

public interface IPrestamoDao extends CrudRepository<Prestamo, Long> {

	public List<Prestamo> findByDevolucion(String devolucion);

	@Query("select p from Prestamo p join fetch p.usuario pe join fetch p.libro l")
	public List<Prestamo> fetchByIdWithLibroWithUsuario();

}
