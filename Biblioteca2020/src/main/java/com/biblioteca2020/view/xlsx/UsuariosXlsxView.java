package com.biblioteca2020.view.xlsx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

@Component("/usuarios/listar.xlsx")
public class UsuariosXlsxView extends AbstractXlsxView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<Usuario> usuariosList = (List<Usuario>) model.get("usuarios");

        Sheet sheet = workbook.createSheet("Reporte de Usuarios");
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
        for (Usuario usuarioItem : usuariosList) {
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
        response.setHeader("Content-Disposition", "attachment; filename=\"listado-usuarios-total.xlsx\"");
    }
}