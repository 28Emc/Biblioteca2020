package com.biblioteca2020.view.pdf;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Prestamo;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
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
                // INICIO A ARMAR MI DOCUMENTO PDF
                document.setMargins(39f, 39f, 0f, 0f);
                document.open();
                // TABLA PARA LA CABECERA
                PdfPTable tablaCabecera = new PdfPTable(2);
                tablaCabecera.setWidthPercentage(120);
                tablaCabecera.setSpacingAfter(20f);
                PdfPCell cellCabecera = null;
                // TITULO
                Font fontTitulo = new Font(new Font(Font.BOLD, 25, Font.NORMAL, new Color(255, 255, 255)));
                cellCabecera = new PdfPCell(new Phrase("Reporte de Préstamos", fontTitulo));
                cellCabecera.setBorderWidth(0);
                cellCabecera.setNoWrap(true);
                cellCabecera.setPaddingTop(35f);
                cellCabecera.setPaddingLeft(25f);
                cellCabecera.setPaddingBottom(30f);
                cellCabecera.setBackgroundColor(new Color(0, 102, 153));
                tablaCabecera.addCell(cellCabecera);
                // IMAGEN
                // NOTA: REVISAR LA IMAGEN (MEJORAR CALIDAD Y TAMAÑO)
                // NOTA 2: VER OTRA MANERA DE MAPEAR LA IMAGEN
                Image image = Image.getInstance("src/main/resources/static/img/logo-pdf.png");
                cellCabecera = new PdfPCell(image);
                cellCabecera.setBorderWidth(0);
                cellCabecera.setNoWrap(true);
                cellCabecera.setPaddingTop(10f);
                cellCabecera.setPaddingLeft(163f);
                cellCabecera.setPaddingBottom(20f);
                cellCabecera.setBackgroundColor(new Color(0, 102, 153));
                tablaCabecera.addCell(cellCabecera);
                document.add(tablaCabecera);
                // ARMO LA TABLA PRINCIPAL QUE VA A ALBERGAR MI LISTADO
                PdfPTable tabla = new PdfPTable(6);
                // ALGUNAS PROPIEDADES
                tabla.setWidths(new float[] { 2, 2, 2, 2, 2, 2 });
                tabla.setWidthPercentage(110);
                PdfPCell cell = null;
                // ARMO UNA CELDA DE CABECERA PARA CADA TIPO DE DATO DEL PRÉSTAMO
                // TAMBIÉN MEJORO EL DISEÑO DE LA CELDA
                Font fontCabeceraTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
                Font fontCuerpoTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
                /*
                 * cell = new PdfPCell(new Phrase("Empleado", fontCabeceraTabla));
                 * cell.setBackgroundColor(new Color(52, 58, 64)); cell.setColspan(1);
                 * cell.setPadding(6f); cell.setBorderWidth(0); tabla.addCell(cell); cell = new
                 * PdfPCell(new Phrase("Libro", fontCabeceraTabla)); cell.setBackgroundColor(new
                 * Color(52, 58, 64)); cell.setColspan(3); cell.setPadding(6f);
                 * cell.setBorderWidth(0); tabla.addCell(cell); cell = new PdfPCell(new
                 * Phrase("Usuario", fontCabeceraTabla)); cell.setBackgroundColor(new Color(52,
                 * 58, 64)); cell.setColspan(2); cell.setPadding(6f); cell.setBorderWidth(0);
                 * tabla.addCell(cell);
                 */
                // AGREGO CABECERAS MAS ESPECÍFICAS
                cell = new PdfPCell(new Phrase("Empleado", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Titulo", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Autor", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Categoría", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("Usuario", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                cell = new PdfPCell(new Phrase("DNI Usuario", fontCabeceraTabla));
                cell.setBackgroundColor(new Color(52, 58, 64));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0);
                tabla.addCell(cell);
                // RECORRO MI LISTA PARA OBTENER LOS VARIOS PRESTAMOS
                for (Prestamo prestamoItem : prestamos) {
                        // AGREGO LA DATA COMO TAL EN CELDAS
                        cell = new PdfPCell(new Phrase(prestamoItem.getEmpleado().getNombres() + ", "
                                        + prestamoItem.getEmpleado().getApellidos(), fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getTitulo(), fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getAutor(), fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getCategoria().getNombre(),
                                        fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getUsuario().getNombres() + ", "
                                        + prestamoItem.getUsuario().getApellidos(), fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                        cell = new PdfPCell(new Phrase(prestamoItem.getUsuario().getNroDocumento(), fontCuerpoTabla));
                        cell.setBorderWidth(0);
                        cell.setPadding(6f);
                        // cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tabla.addCell(cell);
                }
                // AL FINAL, AGREGO MI TABLA AL DOCUMENTO PDF
                document.add(tabla);
                document.close();
        }

}