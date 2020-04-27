package com.biblioteca2020.view.pdf;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.biblioteca2020.models.entity.Prestamo;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;

public class GenerarReportePDF {

    // MÈTODO PARA GENERAR ARCHIVOS PDF CON DATA DINÁMICA
    public static ByteArrayInputStream prestamosTotales(List<Prestamo> prestamos) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // INICIO A ARMAR MI DOCUMENTO PDF
        PdfWriter.getInstance(document, out);
        document.setMargins(39f, 39f, 0f, 0f);
        document.open();
        document.addTitle("Biblioteca2020 || Listado de Préstamos");
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
        // AGREGO CABECERAS MAS ESPECÍFICAS
        // CREANDO CABECERA
        ArrayList<String> listCabecera = new ArrayList<>();
        listCabecera.add("Titulo");
        listCabecera.add("Empleado");
        listCabecera.add("Autor");
        listCabecera.add("Categoría");
        listCabecera.add("Usuario");
        listCabecera.add("DNI Usuario");
        for (String cabeceraItem : listCabecera) {
            cell = new PdfPCell(new Phrase(cabeceraItem, fontCabeceraTabla));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(6f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }
        // RECORRO MI LISTA PARA OBTENER LOS VARIOS PRESTAMOS
        for (Prestamo prestamoItem : prestamos) {
            // AGREGO LA DATA COMO TAL EN CELDAS
            // NOTA 3: REVISAR LOS CAMPOS A AGREGAR AL REPORTE
            cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getTitulo(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(
                    prestamoItem.getEmpleado().getNombres() + ", " + prestamoItem.getEmpleado().getApellidos(),
                    fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getAutor(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getCategoria().getNombre(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
            cell = new PdfPCell(
                    new Phrase(prestamoItem.getUsuario().getNombres() + ", " + prestamoItem.getUsuario().getApellidos(),
                            fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getUsuario().getNroDocumento(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(6f);
            tabla.addCell(cell);
        }
        // AL FINAL, AGREGO MI TABLA AL DOCUMENTO PDF
        document.add(tabla);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}