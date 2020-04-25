package com.biblioteca2020.view.xlsx;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Prestamo;
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
        List<Prestamo> prestamos = (List<Prestamo>) model.get("prestamos");
        // CREANDO PLANTILLA DE EXCEL
        Sheet sheet = workbook.createSheet("Reporte de Préstamos");
        // CREANDO FILAS Y COLUMNAS
        // TITULO
        Row titulo = sheet.createRow(0);
        titulo.createCell(0).setCellValue("Reporte de Préstamos");
        // TABLA
        // CABECERA
        /*CellStyle cellStyleCabecera = workbook.createCellStyle();
        cellStyleCabecera.setFillForegroundColor(IndexedColors.BLACK.index);*/
        Row cabecera = sheet.createRow(2);
        //cabecera.setRowStyle(cellStyleCabecera);

        cabecera.createCell(0).setCellValue("Id");
        cabecera.createCell(1).setCellValue("Titulo Libro");
        cabecera.createCell(2).setCellValue("Autor Libro");
        cabecera.createCell(3).setCellValue("Categoría Libro");
        cabecera.createCell(4).setCellValue("Nombre Completo Empleado");
        cabecera.createCell(5).setCellValue("Nombre Completo Usuario");
        cabecera.createCell(6).setCellValue("DNI Usuario");
        cabecera.createCell(7).setCellValue("Fecha Despacho");
        cabecera.createCell(8).setCellValue("Fecha Devolución");
        cabecera.createCell(9).setCellValue("Estado");
        // cabecera.createCell(7).setCellValue("Observaciones");
        // CUERPO
        //CellStyle cellStyleCuerpo = workbook.createCellStyle();
        int rowNum = 3; // NRO. DE FILA QUE VA JUSTO DEBAJO DE LA CABECERA
        // RECORRO MI LISTA DE PRESTAMOS PARA RELLENAR CADA CELDA DE LA FILA
        for (Prestamo prestamo : prestamos) {
            Row fila = sheet.createRow(rowNum++);
            fila.createCell(0).setCellValue(prestamo.getId());
            fila.createCell(1).setCellValue(prestamo.getLibro().getTitulo());
            fila.createCell(2).setCellValue(prestamo.getLibro().getAutor());
            fila.createCell(3).setCellValue(prestamo.getLibro().getCategoria().getNombre());
            fila.createCell(4)
                    .setCellValue(prestamo.getEmpleado().getNombres() + ", " + prestamo.getEmpleado().getApellidos());
            fila.createCell(5)
                    .setCellValue(prestamo.getUsuario().getNombres() + ", " + prestamo.getUsuario().getApellidos());
            fila.createCell(6).setCellValue(prestamo.getUsuario().getNroDocumento());
            fila.createCell(7).setCellValue(prestamo.getFecha_despacho().toString());
            fila.createCell(8).setCellValue(prestamo.getFecha_devolucion().toString());
            if (prestamo.getDevolucion()) {
                fila.createCell(9)
                        .setCellValue("PRESTAMO COMPLETADO (" + prestamo.getDevolucion().booleanValue() + ")");
            } else {
                fila.createCell(9).setCellValue("LIBRO SIN DEVOLVER (" + prestamo.getDevolucion().booleanValue() + ")");
            }
            // fila.createCell(7).setCellValue(prestamo.getObservaciones());
        }
    }
}