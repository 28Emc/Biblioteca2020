package com.biblioteca2020.models.service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.ILibroDao;
import com.biblioteca2020.models.dao.IPrestamoDao;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;

@Service
public class PrestamoServiceImpl implements IPrestamoService {

	@Autowired
	private IPrestamoDao prestamoDao;

	@Autowired
	private ILibroDao libroDao;

	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> findAll() {
		return (List<Prestamo>) prestamoDao.findAll();
	}

	@Override
	@Transactional
	public void save(Prestamo prestamo) {
		prestamoDao.save(prestamo);
	}

	@Override
	@Transactional
	public void update(Prestamo prestamo) {
		prestamoDao.save(prestamo);
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public Prestamo findById(Long id) {
		return prestamoDao.findById(id).orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> findByDevolucion(String devolucion) {
		return prestamoDao.findByDevolucion(devolucion);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Libro> findByTitulo(String term) {
		return libroDao.findByTitulo("%" + term + "%");
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(Long idEmpleado) {
		return prestamoDao.fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(idEmpleado);
	}
	// USADO
	@Override
	public String mostrarFechaAmigable() {
		// ARMANDO FECHA MAS AMIGABLE AL USUARIO CON TIME
		Locale esp = new Locale("es", "PE");
		
		// Obtienes el dia, mes y año actuales
		String diaNum = String.valueOf(LocalDate.now().getDayOfMonth());
		
		// Mejorando cadena de dia
		String dia = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, esp);
		String diaMayus = dia.substring(0,1).toUpperCase();
		String demasLetrasDia = dia.substring(1, dia.length());
		String diaFinal = diaMayus + demasLetrasDia;
		
		// Mejorando cadena de mes
		String mes = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp);
		String mesMayus = mes.substring(0,1).toUpperCase();
		String demasLetrasMes = mes.substring(1, mes.length());
		String mesFinal = mesMayus + demasLetrasMes;
		
		String anio = String.valueOf(LocalDate.now().getYear());
		
		String fechaFull = diaFinal + " " + diaNum + " de " + mesFinal + " " + anio;
		return fechaFull;
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(Long id) {
		return prestamoDao.fetchByIdWithLibroWithUsuarioWithEmpleadoPerUser(id);
	}
	// USADO
	@Override
	@Transactional(readOnly = true)
	public List<Prestamo> fetchByIdWithLibroWithUsuarioWithEmpleado() {
		return prestamoDao.fetchByIdWithLibroWithUsuarioWithEmpleado();
	}
}
