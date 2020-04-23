package com.biblioteca2020.view.pdf;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Prestamo;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

@Component("prestamos/cargarPdf")
public class PrestamosPdfView extends AbstractPdfView {

    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        Prestamo prestamo = (Prestamo) model.get("prestamo");

        PdfPTable tabla = new PdfPTable(1);
        tabla.addCell("Datos Del Préstamo");
        tabla.addCell(
                "Empleado: " + prestamo.getEmpleado().getNombres() + "  " + prestamo.getEmpleado().getApellidos());

        PdfPTable tabla2 = new PdfPTable(1);
        tabla2.addCell("Datos Del Libro");
        tabla2.addCell("Titulo: " + prestamo.getLibro().getTitulo());
        tabla2.addCell("Autor: " + prestamo.getLibro().getAutor());
        tabla2.addCell("Categoría: " + prestamo.getLibro().getCategoria().getNombre());

        PdfPTable tabla3 = new PdfPTable(1);
        tabla3.addCell("Datos Del Usuario");
        tabla3.addCell("Nombre Completo: " + prestamo.getUsuario().getNombres() + " - "
                + prestamo.getUsuario().getApellidos());
        tabla3.addCell("DNI: " + prestamo.getUsuario().getNroDocumento());

        document.add(tabla);
        document.add(tabla2);
        document.add(tabla3);
    }

}