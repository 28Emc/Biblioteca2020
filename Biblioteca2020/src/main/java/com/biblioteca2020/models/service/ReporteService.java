package com.biblioteca2020.models.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Prestamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class ReporteService {

    @Autowired
    private IPrestamoService prestamoService;

    @Autowired
    private IEmpleadoService empleadoService;

    public String exportarReportePrestamos(String formato, String role, Authentication authentication)
            throws FileNotFoundException, JRException {
        // OBTENER USUARIO LOGUEADO ACTUALMENTE
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Empleado empleado = empleadoService.findByUsername(userDetails.getUsername());
        List<Prestamo> prestamos = null;
        // VERIFICAR ROL
        role = userDetails.getAuthorities().toString();
        switch (role) {
            // ADMIN: VE PRESTAMOS DEL LOCAL ENTERO
            case "[ROLE_ADMIN]":
                prestamos = prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado(empleado.getLocal().getId());
                break;
            // EMPLEADO: VE PRESTAMOS REALIZADOS A SU NOMBRE
            case "[ROLE_EMPLEADO]":
                prestamos = prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(empleado.getId());
                break;
        }
        // MAPEO PLANTILLA DEL REPORTE
        File file = ResourceUtils.getFile("classpath:templates/prestamos/reportes/prestamos.jrxml");
        String pathReportes = "C:\\Users\\Edi\\Desktop\\Reportes";
        // LÓGICA DE PREPARACIÓN REPORTE:
        // COMPILO MI PLANTILLA ..
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        // .. LE ASIGNO UN ORIGEN DE DATOS ..
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(prestamos);
        // .. ASIGNO PARAMETROS OPCIONALES ..
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("titulo", "Biblioteca || Reporte de Préstamos");
        // .. Y AL FINAL EXPORTO MI REPORTE.
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        // EN HTML ..
        if (formato.equalsIgnoreCase("html")) {
            JasperExportManager.exportReportToHtmlFile(jasperPrint, pathReportes + "\\prestamosTotal.html");
        }
        // .. O PDF.
        if (formato.equalsIgnoreCase("pdf")) {
            JasperExportManager.exportReportToPdfFile(jasperPrint, pathReportes + "\\prestamosTotal.pdf");
        }
        // MUESTRO UN MENSAJE DE CONFIRMACION
        return "El reporte ha sido generado en formato '" + formato + "' en la carpeta!";
    }
}