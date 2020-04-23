package com.biblioteca2020.view.pdf;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Prestamo;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

@Component("/prestamos/listar")
public class PrestamosPdfView extends AbstractPdfView {
        // MÉTODO PARA GENERAR PDF
        @Override
        protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {
                document.addTitle("Biblioteca2020 || Listado de Préstamos");
                // LISTADO DE PRÉSTAMOS QUE RECOJO DESDE CONTROLADOR
                @SuppressWarnings("unchecked")
                List<Prestamo> prestamos = (List<Prestamo>) model.get("prestamos");                               
                // ARMO LA TABLA QUE VA A ALBERGAR MI LISTADO
                PdfPTable tabla = new PdfPTable(6);
                tabla.setWidths(new float[] { 2, 2, 2, 2, 2, 2 });
                PdfPCell cell = null;
                // ARMO UNA CELDA DE CABECERA PARA CADA TIPO DE DATO DEL PRÉSTAMO
                // TAMBIÉN MEJORO EL DISEÑO DE LA CELDA
                Font fontCabecera = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
                Font fontCuerpo = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
                cell = new PdfPCell(new Phrase("Empleado", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setColspan(1);
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Libro", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setColspan(3);
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Usuario", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setColspan(2);
                cell.setPadding(6f);
                tabla.addCell(cell);
                // AGREGO CABECERAS MAS ESPECÍFICAS
                cell = new PdfPCell(new Phrase("Nombre Completo", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Titulo", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Autor", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Categoría", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Nombre Completo", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("DNI", fontCabecera));
                cell.setBackgroundColor(new Color(69, 77, 85));
                cell.setPadding(6f);
                tabla.addCell(cell);
                // RECORRO MI LISTA PARA OBTENER LOS VARIOS PRESTAMOS
                for (Prestamo prestamoItem : prestamos) {
                        // AGREGO LA DATA COMO TAL EN CELDAS
                        cell = new PdfPCell(new Phrase(prestamoItem.getEmpleado().getNombres() + ", "
                                        + prestamoItem.getEmpleado().getApellidos(), fontCuerpo));
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getTitulo(), fontCuerpo));
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getAutor(), fontCuerpo));
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getCategoria().getNombre(), fontCuerpo));
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getUsuario().getNombres() + ", "
                                        + prestamoItem.getUsuario().getApellidos(), fontCuerpo));
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getUsuario().getNroDocumento(), fontCuerpo));
                        tabla.addCell(cell);
                }
                // AL FINAL, AGREGO MI TABLA AL DOCUMENTO PDF
                document.add(tabla);
        }

}