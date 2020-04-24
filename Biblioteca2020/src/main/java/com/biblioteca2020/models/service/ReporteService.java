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
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
public class ReporteService {

    @Autowired
    private IPrestamoService prestamoService;

    @Autowired
    private IEmpleadoService empleadoService;

    private File file = null;
    private JasperReport jasperReport = null;
    private JasperPrint jasperPrint = null;
    private JRBeanCollectionDataSource dataSource = null;

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
        String pathReportes = "C:\\Users\\Edi\\Desktop\\Reportes";
        // ORIGEN DE DATOS (EL LISTADO DE MI SERVICIO) QUE VA A RELLENAR MI REPORTE
        dataSource = new JRBeanCollectionDataSource(prestamos);
        // .. ASIGNO PARAMETROS OPCIONALES ..
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("titulo", "Reporte de Préstamos");
        if (formato.equalsIgnoreCase("pdf")) {
            // LÓGICA DE PREPARACIÓN REPORTE:
            // MAPEO PLANTILLA DEL REPORTE ..
            file = ResourceUtils.getFile("classpath:templates/prestamos/reportes/prestamos.jrxml");
            // .. COMPILO MI PLANTILLA ..
            jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
            // .. IMPRIMO MI REPORTE CON LA PLANTILLA, PARAMETROS OPCIONALES Y LE ASIGNO MI
            // ORIGEN DE DATOS ..
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            // .. Y AL FINAL EXPORTO EL REPORTE.
            JasperExportManager.exportReportToPdfFile(jasperPrint, pathReportes + "\\prestamosTotal.pdf");
        }
        if (formato.equalsIgnoreCase("xlsx")) {
            // LÓGICA PARECIDA A LA ANTERIOR, PERO UTILIZO CLASE ESPECIFICA PARA EXPORTAR
            // XLSX
            file = ResourceUtils.getFile("classpath:templates/prestamos/reportes/prestamosExcel.jrxml");
            jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, pathReportes + "\\prestamosTotal.xlsx");
            
            JRXlsxExporter exporter = new JRXlsxExporter();
            // DEFINO MI ORIGEN DE DATOS ..
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            // .. DEFINO DONDE SE VA A EXPORTAR MI ARCHIVO EXCEL..
            File outputFile = new File(pathReportes + "\\prestamosTotal.xlsx");
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));
            // .. APLICO PROPIEDADES OPCIONALES AL ARCHIVO EXCEL .. 
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setCollapseRowSpan(true);
            configuration.setOnePagePerSheet(true);
            exporter.setConfiguration(configuration);
            // .. Y AL FINAL EXPORTO MI REPORTE.
            exporter.exportReport();
        }
        // MUESTRO UN MENSAJE DE CONFIRMACION
        return "El reporte ha sido generado en formato '" + formato + "' en la carpeta '" + pathReportes + "'";
    }

    // public String exportarReporteExcelPrestamos(String formato, String role,
    // Authentication authentication)
    // throws FileNotFoundException, JRException {
    // // OBTENER USUARIO LOGUEADO ACTUALMENTE
    // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    // Empleado empleado =
    // empleadoService.findByUsername(userDetails.getUsername());
    // List<Prestamo> prestamos = null;

    // File fileExcel = null;
    // JasperReport jasperReportExcel = null;
    // JasperPrint jasperPrintExcel = null;

    // // VERIFICAR ROL
    // role = userDetails.getAuthorities().toString();
    // switch (role) {
    // // ADMIN: VE PRESTAMOS DEL LOCAL ENTERO
    // case "[ROLE_ADMIN]":
    // prestamos =
    // prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleado(empleado.getLocal().getId());
    // break;
    // // EMPLEADO: VE PRESTAMOS REALIZADOS A SU NOMBRE
    // case "[ROLE_EMPLEADO]":
    // prestamos =
    // prestamoService.fetchByIdWithLibroWithUsuarioWithEmpleadoPerEmpleado(empleado.getId());
    // break;
    // }
    // // MAPEO PLANTILLA DEL REPORTE
    // fileExcel =
    // ResourceUtils.getFile("classpath:templates/prestamos/reportes/prestamosExcel.jrxml");
    // String pathReportes = "C:\\Users\\Edi\\Desktop\\Reportes";
    // // LÓGICA DE PREPARACIÓN REPORTE:
    // // COMPILO MI PLANTILLA ..
    // jasperReportExcel =
    // JasperCompileManager.compileReport(fileExcel.getAbsolutePath());
    // // .. LE ASIGNO UN ORIGEN DE DATOS ..
    // JRBeanCollectionDataSource dataSource = new
    // JRBeanCollectionDataSource(prestamos);
    // // .. ASIGNO PARAMETROS OPCIONALES ..
    // Map<String, Object> parameters = new HashMap<>();
    // parameters.put("titulo", "Reporte de Préstamos");
    // // .. Y AL FINAL EXPORTO MI REPORTE ..
    // jasperPrintExcel = JasperFillManager.fillReport(jasperReportExcel,
    // parameters, dataSource);
    // // .. EN EXCEL.
    // if (formato.equalsIgnoreCase("xlsx")) {
    // JRXlsxExporter exporter = new JRXlsxExporter();
    // exporter.setExporterInput(new SimpleExporterInput(jasperPrintExcel));
    // File outputFile = new File(pathReportes + "\\prestamosTotal.xlsx");
    // exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));

    // SimpleXlsxReportConfiguration configuration = new
    // SimpleXlsxReportConfiguration();
    // // configuration.setDetectCellType(true); //Setconfiguration as you like it!!
    // configuration.setCollapseRowSpan(true);
    // configuration.setOnePagePerSheet(true);
    // // configuration.setIgnoreGraphics(true);
    // exporter.setConfiguration(configuration);

    // exporter.exportReport();
    // }
    // // MUESTRO UN MENSAJE DE CONFIRMACION
    // return "El reporte ha sido generado en formato '" + formato + "' en la
    // carpeta '" + pathReportes + "'.";
    // }
}