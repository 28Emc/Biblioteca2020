package com.biblioteca2020.view.xlsx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Prestamo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

@Component("/prestamos/listar.xlsx")
public class PrestamosXlsxView extends AbstractXlsxView {
    // MÉTODO PARA GENERAR ARCHIVO EXCEL
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<Prestamo> prestamosList = (List<Prestamo>) model.get("prestamos");
        try {
            String titulo = (String) model.get("tituloReporte");
            String nombre = (String) model.get("nombreReporte");
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
            for (Prestamo prestamoItem : prestamosList) {
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
            // ASIGNANDO NOMBRE AL ARCHIVO EXCEL
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + ".xlsx\"");
        } catch (Exception e) {
            response.setHeader("Location", "/error_reporte");
            e.getMessage();
        }

        /*
         * String titulo = (String) model.get("tituloReporte"); String nombre = (String)
         * model.get("nombreReporte"); // CREANDO PLANTILLA DE EXCEL Sheet sheet =
         * workbook.createSheet(titulo); int rowNum = 0; int cellNum = 0; // CREANDO
         * FUENTE Font font = workbook.createFont(); font.setFontHeightInPoints((short)
         * 10); font.setFontName("Serif");
         * font.setColor(IndexedColors.WHITE.getIndex()); font.setBold(true);
         * font.setItalic(false); // CREANDO ESTILO CellStyle style =
         * workbook.createCellStyle();
         * style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         * style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.index);
         * style.setAlignment(HorizontalAlignment.CENTER); // SETEANDO LA FUENTE AL
         * ESTILO style.setFont(font); // ##################### TABLA // CREANDO
         * CABECERA ArrayList<String> listCabecera = new ArrayList<>();
         * listCabecera.add("Id"); listCabecera.add("Titulo");
         * listCabecera.add("Autor"); listCabecera.add("Categoría");
         * listCabecera.add("Empleado"); listCabecera.add("Usuario");
         * listCabecera.add("Fecha Despacho"); listCabecera.add("Fecha Devolución");
         * listCabecera.add("Estado"); // ITERANDO LISTA DE CABECERA PARA CREAR CELDAS Y
         * APLICAR ESTILOS Row cabecera = sheet.createRow(rowNum); for (String
         * cabeceraItem : listCabecera) { Cell cell = cabecera.createCell(cellNum++);
         * cell.setCellValue(cabeceraItem); // CONDICONAL IF OPCIONAL, PERO LA DEJO PARA
         * AGREGAR MAS ELEMENTOS ANTES // DE ESTA FILA EN UN FUTURO if (rowNum == 0) {
         * cell.setCellStyle(style); } } // CUERPO rowNum = 1; // NRO. DE FILA QUE VA
         * JUSTO DEBAJO DE LA CABECERA // RECORRO MI LISTA DE PRESTAMOS PARA RELLENAR
         * CADA CELDA DE LA FILA for (Prestamo prestamoItem : prestamosList) { Row fila
         * = sheet.createRow(rowNum++);
         * fila.createCell(0).setCellValue(prestamoItem.getId());
         * fila.createCell(1).setCellValue(prestamoItem.getLibro().getTitulo());
         * fila.createCell(2).setCellValue(prestamoItem.getLibro().getAutor());
         * fila.createCell(3).setCellValue(prestamoItem.getLibro().getCategoria().
         * getNombre()); fila.createCell(4).setCellValue(
         * prestamoItem.getEmpleado().getNombres() + ", " +
         * prestamoItem.getEmpleado().getApellidos()); fila.createCell(5).setCellValue(
         * prestamoItem.getUsuario().getNombres() + ", " +
         * prestamoItem.getUsuario().getApellidos());
         * fila.createCell(6).setCellValue(prestamoItem.getFecha_despacho().toString());
         * fila.createCell(7).setCellValue(prestamoItem.getFecha_devolucion().toString()
         * ); if (prestamoItem.getDevolucion()) { fila.createCell(8)
         * .setCellValue("Préstamo completado (" +
         * prestamoItem.getDevolucion().booleanValue() + ")"); } else {
         * fila.createCell(8) .setCellValue("Libro sin devolver (" +
         * prestamoItem.getDevolucion().booleanValue() + ")"); } } // AJUSTO EL ANCHO DE
         * CADA CELDA PARA MOSTRAR MEJOR LA DATA // NOTA: ESTA ITERACIÓN PUEDE CAUSAR
         * BAJO RENDIMIENTO A LA HORA DE ABRIR EL // ARCHIVO for (int i = 0; i <
         * listCabecera.size(); i++) { sheet.autoSizeColumn(i); } // ASIGNANDO NOMBRE AL
         * ARCHIVO EXCEL response.setHeader("Content-Disposition",
         * "attachment; filename=\"" + nombre + ".xlsx\"");
         */
    }
}