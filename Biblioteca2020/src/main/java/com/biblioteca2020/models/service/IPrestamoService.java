package com.biblioteca2020.models.service;

import java.util.List;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;

public interface IPrestamoService {
	
	public List<Prestamo> findAll();

	public void save(Prestamo prestamo);

	public Prestamo findById(Long id);
	
	public List<Prestamo> findByDevolucion(String devolucion);
	
	public List<Prestamo> fetchPrestamoByIdWithLibroWithUsuario();
	
	public List<Libro> findByTitulo(String term);

}
