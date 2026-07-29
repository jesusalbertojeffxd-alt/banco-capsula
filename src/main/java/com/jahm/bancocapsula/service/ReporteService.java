package com.jahm.bancocapsula.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ============================================================
    // REPORTE DE CREDITOS POR CLIENTE
    // ============================================================
    public ByteArrayOutputStream generarReporteCreditos(UsuarioEntity cliente, List<SolicitudCreditoEntity> creditos) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            Paragraph titulo = new Paragraph("REPORTE DE CREDITOS")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GREEN);
            document.add(titulo);

            document.add(new Paragraph(" ").setHeight(10));

            Table infoCliente = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoCliente.setWidth(UnitValue.createPercentValue(100));

            agregarFila(infoCliente, "Cliente:", cliente.getNombre());
            agregarFila(infoCliente, "Usuario:", cliente.getUsername());
            agregarFila(infoCliente, "Rol:", cliente.getRol());
            agregarFila(infoCliente, "Fecha Reporte:", LocalDateTime.now().format(FORMATTER));

            document.add(infoCliente);
            document.add(new Paragraph(" ").setHeight(10));

            Table tablaCreditos = new Table(UnitValue.createPercentArray(new float[]{20, 25, 25, 30}));
            tablaCreditos.setWidth(UnitValue.createPercentValue(100));

            agregarCeldaHeader(tablaCreditos, "Fecha");
            agregarCeldaHeader(tablaCreditos, "Monto");
            agregarCeldaHeader(tablaCreditos, "Estado");
            agregarCeldaHeader(tablaCreditos, "Fecha Aprobacion");

            for (SolicitudCreditoEntity credito : creditos) {
                tablaCreditos.addCell(new Cell().add(new Paragraph(credito.getFecha().format(FORMATTER))));
                tablaCreditos.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", credito.getMontoSolicitado()))));
                tablaCreditos.addCell(new Cell().add(new Paragraph(credito.getEstado())));
                tablaCreditos.addCell(new Cell().add(new Paragraph(credito.getFecha() != null ? credito.getFecha().format(FORMATTER) : "N/A")));
            }

            document.add(tablaCreditos);
            document.close();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando reporte de creditos: " + e.getMessage());
        }
    }

    // ============================================================
    // REPORTE DE MOVIMIENTOS POR CLIENTE
    // ============================================================
    public ByteArrayOutputStream generarReporteMovimientos(UsuarioEntity cliente, List<MovimientoEntity> movimientos) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            Paragraph titulo = new Paragraph("REPORTE DE MOVIMIENTOS")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GREEN);
            document.add(titulo);

            document.add(new Paragraph(" ").setHeight(10));

            Table infoCliente = new Table(
