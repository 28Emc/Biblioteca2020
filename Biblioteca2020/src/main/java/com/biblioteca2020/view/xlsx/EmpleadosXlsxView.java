package com.biblioteca2020.view.xlsx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.biblioteca2020.models.entity.Empleado;
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

@Component("/empleados/listar.xlsx")
public class EmpleadosXlsxView extends AbstractXlsxView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<Empleado> empleadoList = (List<Empleado>) model.get("empleados");

        Sheet sheet = workbook.createSheet("Reporte de Empleados");
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
        for (Empleado empleadoItem : empleadoList) {
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
        response.setHeader("Content-Disposition", "attachment; filename=\"listado-empleados-total.xlsx\"");
    }
}