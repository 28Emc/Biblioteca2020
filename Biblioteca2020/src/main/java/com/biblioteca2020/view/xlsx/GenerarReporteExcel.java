package com.biblioteca2020.view.xlsx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.Usuario;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// MÈTODO PARA GENERAR ARCHIVOS EXCEL CON DATA DINÁMICA
public class GenerarReporteExcel {

    // ################ PRESTAMOS
    public static ByteArrayInputStream generarExcelPrestamos(String titulo, List<Prestamo> prestamos)
            throws IOException {
        // int columns = datos.size();//para generar total de columnas por ID
        // encontrados
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            // CREANDO PLANTILLA DE EXCEL
            Sheet sheet = workbook.createSheet(titulo);
            int rowNum = 0;
            int cellNum = 0;
            // CREANDO FUENTE
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("Serif");
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            font.setItalic(false);
            // CREANDO ESTILO
            CellStyle style = workbook.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.index);
            style.setAlignment(HorizontalAlignment.CENTER);
            // SETEANDO LA FUENTE AL ESTILO
            style.setFont(font);
            // ##################### TABLA
            // CREANDO CABECERA
            ArrayList<String> listCabecera = new ArrayList<>();
            listCabecera.add("Id");
            listCabecera.add("Titulo");
            listCabecera.add("Autor");
            listCabecera.add("Categoría");
            listCabecera.add("Empleado");
            listCabecera.add("Usuario");
            listCabecera.add("Fecha Despacho");
            listCabecera.add("Fecha Devolución");
            listCabecera.add("Estado");
            // ITERANDO LISTA DE CABECERA PARA CREAR CELDAS Y APLICAR ESTILOS
            Row cabecera = sheet.createRow(rowNum);
            for (String cabeceraItem : listCabecera) {
                Cell cell = cabecera.createCell(cellNum++);
                cell.setCellValue(cabeceraItem);
                // CONDICONAL IF OPCIONAL, PERO LA DEJO PARA AGREGAR MAS ELEMENTOS ANTES
                // DE ESTA FILA EN UN FUTURO
                if (rowNum == 0) {
                    cell.setCellStyle(style);
                }
            }
            // CUERPO
            rowNum = 1; // NRO. DE FILA QUE VA JUSTO DEBAJO DE LA CABECERA
            // RECORRO MI LISTA DE PRESTAMOS PARA RELLENAR CADA CELDA DE LA FILA
            for (Prestamo prestamoItem : prestamos) {
                Row fila = sheet.createRow(rowNum++);
                fila.createCell(0).setCellValue(prestamoItem.getId());
                fila.createCell(1).setCellValue(prestamoItem.getLibro().getTitulo());
                fila.createCell(2).setCellValue(prestamoItem.getLibro().getAutor());
                fila.createCell(3).setCellValue(prestamoItem.getLibro().getCategoria().getNombre());
                fila.createCell(4).setCellValue(
                        prestamoItem.getEmpleado().getNombres() + ", " + prestamoItem.getEmpleado().getApellidos());
                fila.createCell(5).setCellValue(
                        prestamoItem.getUsuario().getNombres() + ", " + prestamoItem.getUsuario().getApellidos());
                fila.createCell(6).setCellValue(prestamoItem.getFecha_despacho().toString());
                fila.createCell(7).setCellValue(prestamoItem.getFecha_devolucion().toString());
                if (prestamoItem.getDevolucion()) {
                    fila.createCell(8)
                            .setCellValue("Préstamo completado (" + prestamoItem.getDevolucion().booleanValue() + ")");
                } else {
                    fila.createCell(8)
                            .setCellValue("Libro sin devolver (" + prestamoItem.getDevolucion().booleanValue() + ")");
                }
            }
            // AJUSTO EL ANCHO DE CADA CELDA PARA MOSTRAR MEJOR LA DATA
            // NOTA: ESTA ITERACIÓN PUEDE CAUSAR BAJO RENDIMIENTO A LA HORA DE ABRIR EL
            // ARCHIVO
            for (int i = 0; i < listCabecera.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ################ EMPLEADOS
    public static ByteArrayInputStream generarExcelEmpleados(String titulo, List<Empleado> empleados)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(titulo);
            int rowNum = 0;
            int cellNum = 0;
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("Serif");
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            font.setItalic(false);
            CellStyle style = workbook.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.index);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            ArrayList<String> listCabecera = new ArrayList<>();
            listCabecera.add("Id");
            listCabecera.add("Nombres");
            listCabecera.add("Apellidos");
            listCabecera.add("DNI");
            listCabecera.add("Empresa");
            listCabecera.add("F. Registro");
            listCabecera.add("Estado");
            Row cabecera = sheet.createRow(rowNum);
            for (String cabeceraItem : listCabecera) {
                Cell cell = cabecera.createCell(cellNum++);
                cell.setCellValue(cabeceraItem);
                if (rowNum == 0) {
                    cell.setCellStyle(style);
                }
            }
            rowNum = 1;
            for (Empleado empleadoItem : empleados) {
                Row fila = sheet.createRow(rowNum++);
                fila.createCell(0).setCellValue(empleadoItem.getId());
                fila.createCell(1).setCellValue(empleadoItem.getNombres());
                fila.createCell(2).setCellValue(empleadoItem.getApellidos());
                fila.createCell(3).setCellValue(empleadoItem.getNroDocumento());
                fila.createCell(4).setCellValue(empleadoItem.getLocal().getEmpresa().getRazonSocial());
                fila.createCell(5).setCellValue(empleadoItem.getFecha_registro().toString());
                if (empleadoItem.getEstado()) {
                    fila.createCell(6).setCellValue("Activo");
                } else {
                    fila.createCell(6).setCellValue("Inactivo");
                }
            }

            for (int i = 0; i < listCabecera.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ################ USUARIOS
    public static ByteArrayInputStream generarExcelUsuarios(String titulo, List<Usuario> usuarios) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(titulo);
            int rowNum = 0;
            int cellNum = 0;
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("Serif");
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            font.setItalic(false);
            CellStyle style = workbook.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.index);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            ArrayList<String> listCabecera = new ArrayList<>();
            listCabecera.add("Id");
            listCabecera.add("Nombres");
            listCabecera.add("Apellidos");
            listCabecera.add("DNI");
            listCabecera.add("Dirección");
            listCabecera.add("Email");
            listCabecera.add("Celular");
            listCabecera.add("F. Registro");
            listCabecera.add("Username");
            listCabecera.add("Estado");
            Row cabecera = sheet.createRow(rowNum);
            for (String cabeceraItem : listCabecera) {
                Cell cell = cabecera.createCell(cellNum++);
                cell.setCellValue(cabeceraItem);
                if (rowNum == 0) {
                    cell.setCellStyle(style);
                }
            }
            rowNum = 1;
            for (Usuario usuarioItem : usuarios) {
                Row fila = sheet.createRow(rowNum++);
                fila.createCell(0).setCellValue(usuarioItem.getId());
                fila.createCell(1).setCellValue(usuarioItem.getNombres());
                fila.createCell(2).setCellValue(usuarioItem.getApellidos());
                fila.createCell(3).setCellValue(usuarioItem.getNroDocumento());
                fila.createCell(4).setCellValue(usuarioItem.getDireccion());
                fila.createCell(5).setCellValue(usuarioItem.getEmail());
                fila.createCell(6).setCellValue(usuarioItem.getCelular());
                fila.createCell(7).setCellValue(usuarioItem.getFecha_registro().toString());
                fila.createCell(8).setCellValue(usuarioItem.getUsername());
                if (usuarioItem.getEstado()) {
                    fila.createCell(9).setCellValue("Activo");
                } else {
                    fila.createCell(9).setCellValue("Inactivo");
                }
            }

            for (int i = 0; i < listCabecera.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ################ LIBROS
    public static ByteArrayInputStream generarExcelLibros(String titulo, List<Libro> libros) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(titulo);
            int rowNum = 0;
            int cellNum = 0;
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("Serif");
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            font.setItalic(false);
            CellStyle style = workbook.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.index);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            ArrayList<String> listCabecera = new ArrayList<>();
            listCabecera.add("Id");
            listCabecera.add("Titulo");
            listCabecera.add("Autor");
            listCabecera.add("Categoría");
            listCabecera.add("Fecha Publicación");
            listCabecera.add("Fecha Registro");
            listCabecera.add("Stock");
            listCabecera.add("Estado");
            Row cabecera = sheet.createRow(rowNum);
            for (String cabeceraItem : listCabecera) {
                Cell cell = cabecera.createCell(cellNum++);
                cell.setCellValue(cabeceraItem);
                if (rowNum == 0) {
                    cell.setCellStyle(style);
                }
            }
            rowNum = 1;
            for (Libro libroItem : libros) {
                Row fila = sheet.createRow(rowNum++);
                fila.createCell(0).setCellValue(libroItem.getId());
                fila.createCell(1).setCellValue(libroItem.getTitulo());
                fila.createCell(2).setCellValue(libroItem.getAutor());
                fila.createCell(3).setCellValue(libroItem.getCategoria().getNombre());
                fila.createCell(4).setCellValue(libroItem.getFechaPublicacion().toString());
                fila.createCell(5).setCellValue(libroItem.getFechaRegistro().toString());
                fila.createCell(6).setCellValue(libroItem.getStock());
                if (libroItem.getEstado()) {
                    fila.createCell(7).setCellValue("Activo");
                } else {
                    fila.createCell(7).setCellValue("Inactivo");
                }
            }

            for (int i = 0; i < listCabecera.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}