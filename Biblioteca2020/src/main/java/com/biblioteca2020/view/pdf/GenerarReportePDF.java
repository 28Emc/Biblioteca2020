package com.biblioteca2020.view.pdf;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.biblioteca2020.models.entity.Empleado;
import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.Usuario;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;

// MÈTODO PARA GENERAR ARCHIVOS PDF CON DATA DINÁMICA
public class GenerarReportePDF {

    // ################ PRESTAMOS
    public static ByteArrayInputStream generarPDFPrestamos(String titulo, List<Prestamo> prestamos) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // INICIO A ARMAR MI DOCUMENTO PDF
        PdfWriter.getInstance(document, out);
        document.setMargins(39f, 39f, 0f, 0f);
        document.open();
        document.addTitle("Biblioteca2020 || " + titulo);
        // TABLA PARA LA CABECERA
        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(120);
        tablaCabecera.setSpacingAfter(20f);
        PdfPCell cellCabecera = null;
        // TITULO
        Font fontTitulo = new Font(new Font(Font.BOLD, 25, Font.NORMAL, new Color(255, 255, 255)));
        cellCabecera = new PdfPCell(new Phrase(titulo, fontTitulo));
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
        PdfPTable tabla = new PdfPTable(7);
        // ALGUNAS PROPIEDADES
        tabla.setWidths(new float[] { 1, 2.8f, 3, 3, 2.5f, 2.5f, 2.5f });
        tabla.setWidthPercentage(110);
        PdfPCell cell = null;
        // ARMO UNA CELDA DE CABECERA PARA CADA TIPO DE DATO DEL PRÉSTAMO
        // TAMBIÉN MEJORO EL DISEÑO DE LA CELDA
        Font fontCabeceraTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
        Font fontCuerpoTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
        // AGREGO CABECERAS MAS ESPECÍFICAS
        // CREANDO CABECERA
        ArrayList<String> listCabecera = new ArrayList<>();
        listCabecera.add("Id");
        listCabecera.add("Libro");
        listCabecera.add("Empleado");
        listCabecera.add("Usuario");
        listCabecera.add("F. Despacho");
        listCabecera.add("F. Devolución");
        listCabecera.add("Estado");
        for (String cabeceraItem : listCabecera) {
            cell = new PdfPCell(new Phrase(cabeceraItem, fontCabeceraTabla));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(8f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }
        // RECORRO MI LISTA PARA OBTENER LOS VARIOS PRESTAMOS
        for (Prestamo prestamoItem : prestamos) {
            // AGREGO LA DATA COMO TAL EN CELDAS
            // NOTA 3: REVISAR LOS CAMPOS A AGREGAR AL REPORTE
            cell = new PdfPCell(new Phrase(prestamoItem.getId().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getLibro().getTitulo(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(
                    prestamoItem.getEmpleado().getNombres() + ", " + prestamoItem.getEmpleado().getApellidos(),
                    fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(
                    new Phrase(prestamoItem.getUsuario().getNombres() + ", " + prestamoItem.getUsuario().getApellidos()
                            + " (" + prestamoItem.getUsuario().getNroDocumento() + ")", fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getFecha_despacho().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(prestamoItem.getFecha_devolucion().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            if (prestamoItem.getDevolucion()) {
                cell = new PdfPCell(new Phrase("Terminado", fontCuerpoTabla));
            } else {
                cell = new PdfPCell(new Phrase("No Devuelto", fontCuerpoTabla));
            }
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
        }
        // AL FINAL, AGREGO MI TABLA AL DOCUMENTO PDF
        document.add(tabla);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ################ LIBROS
    public static ByteArrayInputStream generarPDFLibros(String titulo, List<Libro> libros) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.setMargins(39f, 39f, 0f, 0f);
        document.open();
        document.addTitle("Biblioteca2020 || " + titulo);

        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(120);
        tablaCabecera.setSpacingAfter(20f);
        PdfPCell cellCabecera = null;
        Font fontTitulo = new Font(new Font(Font.BOLD, 25, Font.NORMAL, new Color(255, 255, 255)));
        cellCabecera = new PdfPCell(new Phrase(titulo, fontTitulo));
        cellCabecera.setBorderWidth(0);
        cellCabecera.setNoWrap(true);
        cellCabecera.setPaddingTop(35f);
        cellCabecera.setPaddingLeft(25f);
        cellCabecera.setPaddingBottom(30f);
        cellCabecera.setBackgroundColor(new Color(0, 102, 153));
        tablaCabecera.addCell(cellCabecera);
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

        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidths(new float[] { 1, 2.5f, 2.3f, 2.3f, 2.5f, 2.3f, 1.3f, 1.8f });
        tabla.setWidthPercentage(110);
        PdfPCell cell = null;
        Font fontCabeceraTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
        Font fontCuerpoTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
        ArrayList<String> listCabecera = new ArrayList<>();
        listCabecera.add("Id");
        listCabecera.add("Titulo");
        listCabecera.add("Autor");
        listCabecera.add("Categoría");
        listCabecera.add("F. Publicación");
        listCabecera.add("F. Registro");
        listCabecera.add("Stock");
        listCabecera.add("Estado");
        for (String cabeceraItem : listCabecera) {
            cell = new PdfPCell(new Phrase(cabeceraItem, fontCabeceraTabla));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(8f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }
        for (Libro libroItem : libros) {
            cell = new PdfPCell(new Phrase(libroItem.getId().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getTitulo(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getAutor(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getCategoria().getNombre(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getFechaPublicacion().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getFechaRegistro().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(libroItem.getStock().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            if (libroItem.getEstado()) {
                cell = new PdfPCell(new Phrase("Activo", fontCuerpoTabla));
            } else {
                cell = new PdfPCell(new Phrase("Inactivo", fontCuerpoTabla));
            }
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
        }
        document.add(tabla);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ################ USUARIOS
    public static ByteArrayInputStream generarPDFUsuarios(String titulo, List<Usuario> usuarios) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.setMargins(39f, 39f, 0f, 0f);
        document.open();
        document.addTitle("Biblioteca2020 || " + titulo);

        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(120);
        tablaCabecera.setSpacingAfter(20f);
        PdfPCell cellCabecera = null;
        Font fontTitulo = new Font(new Font(Font.BOLD, 25, Font.NORMAL, new Color(255, 255, 255)));
        cellCabecera = new PdfPCell(new Phrase(titulo, fontTitulo));
        cellCabecera.setBorderWidth(0);
        cellCabecera.setNoWrap(true);
        cellCabecera.setPaddingTop(35f);
        cellCabecera.setPaddingLeft(25f);
        cellCabecera.setPaddingBottom(30f);
        cellCabecera.setBackgroundColor(new Color(0, 102, 153));
        tablaCabecera.addCell(cellCabecera);
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

        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidths(new float[] { 1, 2.5F, 2.5F, 2, 2.5F, 2, 1.5F });
        tabla.setWidthPercentage(110);
        PdfPCell cell = null;
        Font fontCabeceraTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
        Font fontCuerpoTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
        ArrayList<String> listCabecera = new ArrayList<>();
        listCabecera.add("Id");
        listCabecera.add("Nombres");
        listCabecera.add("Apellidos");
        listCabecera.add("DNI");
        listCabecera.add("F. Registro");
        listCabecera.add("Username");
        listCabecera.add("Estado");
        for (String cabeceraItem : listCabecera) {
            cell = new PdfPCell(new Phrase(cabeceraItem, fontCabeceraTabla));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(8f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }
        for (Usuario usuarioItem : usuarios) {
            cell = new PdfPCell(new Phrase(usuarioItem.getId().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(usuarioItem.getNombres(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(usuarioItem.getApellidos(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(usuarioItem.getNroDocumento(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(usuarioItem.getFecha_registro().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(usuarioItem.getUsername(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            if (usuarioItem.getEstado()) {
                cell = new PdfPCell(new Phrase("Activo", fontCuerpoTabla));
            } else {
                cell = new PdfPCell(new Phrase("Inactivo", fontCuerpoTabla));
            }
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
        }
        document.add(tabla);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ################ EMPLEADOS
    public static ByteArrayInputStream generarPDFEmpleados(String titulo, List<Empleado> empleados) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.setMargins(39f, 39f, 0f, 0f);
        document.open();
        document.addTitle("Biblioteca2020 || " + titulo);

        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(120);
        tablaCabecera.setSpacingAfter(20f);
        PdfPCell cellCabecera = null;
        Font fontTitulo = new Font(new Font(Font.BOLD, 25, Font.NORMAL, new Color(255, 255, 255)));
        cellCabecera = new PdfPCell(new Phrase(titulo, fontTitulo));
        cellCabecera.setBorderWidth(0);
        cellCabecera.setNoWrap(true);
        cellCabecera.setPaddingTop(35f);
        cellCabecera.setPaddingLeft(25f);
        cellCabecera.setPaddingBottom(30f);
        cellCabecera.setBackgroundColor(new Color(0, 102, 153));
        tablaCabecera.addCell(cellCabecera);
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

        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidths(new float[] { 1, 2.5F, 2.5F, 2, 2.5F, 2, 1.5F });
        tabla.setWidthPercentage(110);
        PdfPCell cell = null;
        Font fontCabeceraTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(255, 255, 255)));
        Font fontCuerpoTabla = new Font(new Font(Font.BOLD, 11, Font.NORMAL, new Color(0, 0, 0)));
        ArrayList<String> listCabecera = new ArrayList<>();
        listCabecera.add("Id");
        listCabecera.add("Nombres");
        listCabecera.add("Apellidos");
        listCabecera.add("DNI");
        listCabecera.add("Empresa");
        listCabecera.add("F. Registro");
        listCabecera.add("Estado");
        for (String cabeceraItem : listCabecera) {
            cell = new PdfPCell(new Phrase(cabeceraItem, fontCabeceraTabla));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(8f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }
        for (Empleado empleadoItem : empleados) {
            cell = new PdfPCell(new Phrase(empleadoItem.getId().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(empleadoItem.getNombres(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(empleadoItem.getApellidos(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(empleadoItem.getNroDocumento(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(empleadoItem.getLocal().getEmpresa().getRazonSocial(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            cell = new PdfPCell(new Phrase(empleadoItem.getFecha_registro().toString(), fontCuerpoTabla));
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
            if (empleadoItem.getEstado()) {
                cell = new PdfPCell(new Phrase("Activo", fontCuerpoTabla));
            } else {
                cell = new PdfPCell(new Phrase("Inactivo", fontCuerpoTabla));
            }
            cell.setBorderWidth(0);
            cell.setPadding(10f);
            tabla.addCell(cell);
        }
        document.add(tabla);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}