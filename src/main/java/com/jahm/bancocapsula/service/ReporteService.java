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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // MÉTODO PARA FORMATEAR FECHAS - ACEPTA CUALQUIER TIPO
    // ============================================================
    private String formatearFecha(Object fecha) {
        if (fecha == null) {
            return "N/A";
        }
        try {
            if (fecha instanceof LocalDateTime) {
                return ((LocalDateTime) fecha).format(FORMATTER);
            } else if (fecha instanceof LocalDate) {
                return ((LocalDate) fecha).format(DATE_FORMATTER);
            } else if (fecha instanceof String) {
                return (String) fecha;
            } else {
                return fecha.toString();
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

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
                tablaCreditos.addCell(new Cell().add(new Paragraph(formatearFecha(credito.getFecha()))));
                tablaCreditos.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", credito.getMontoSolicitado()))));
                tablaCreditos.addCell(new Cell().add(new Paragraph(credito.getEstado())));
                tablaCreditos.addCell(new Cell().add(new Paragraph(formatearFecha(credito.getFecha()))));
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

            Table infoCliente = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoCliente.setWidth(UnitValue.createPercentValue(100));

            agregarFila(infoCliente, "Cliente:", cliente.getNombre());
            agregarFila(infoCliente, "Usuario:", cliente.getUsername());
            agregarFila(infoCliente, "Rol:", cliente.getRol());
            agregarFila(infoCliente, "Fecha Reporte:", LocalDateTime.now().format(FORMATTER));

            document.add(infoCliente);
            document.add(new Paragraph(" ").setHeight(10));

            Table tablaMovimientos = new Table(UnitValue.createPercentArray(new float[]{15, 18, 18, 15, 15, 10, 9}));
            tablaMovimientos.setWidth(UnitValue.createPercentValue(100));

            agregarCeldaHeader(tablaMovimientos, "Fecha");
            agregarCeldaHeader(tablaMovimientos, "Cuenta Origen");
            agregarCeldaHeader(tablaMovimientos, "Cuenta Destino");
            agregarCeldaHeader(tablaMovimientos, "Monto");
            agregarCeldaHeader(tablaMovimientos, "Tipo");
            agregarCeldaHeader(tablaMovimientos, "Estado");
            agregarCeldaHeader(tablaMovimientos, "Descripcion");

            for (MovimientoEntity mov : movimientos) {
                tablaMovimientos.addCell(new Cell().add(new Paragraph(formatearFecha(mov.getFecha()))));
                tablaMovimientos.addCell(new Cell().add(new Paragraph(mov.getCuentaOrigen() != null ? mov.getCuentaOrigen() : "N/A")));
                tablaMovimientos.addCell(new Cell().add(new Paragraph(mov.getCuentaDestino() != null ? mov.getCuentaDestino() : "N/A")));
                tablaMovimientos.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", mov.getMonto()))));
                tablaMovimientos.addCell(new Cell().add(new Paragraph(mov.getTipo() != null ? mov.getTipo() : "N/A")));
                tablaMovimientos.addCell(new Cell().add(new Paragraph(mov.getEstadoMovimiento() != null ? mov.getEstadoMovimiento() : "N/A")));
                tablaMovimientos.addCell(new Cell().add(new Paragraph(mov.getDescripcion() != null ? mov.getDescripcion() : "N/A")));
            }

            document.add(tablaMovimientos);
            document.close();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando reporte de movimientos: " + e.getMessage());
        }
    }

    // ============================================================
    // OFICIO DE CREDITO
    // ============================================================
    public ByteArrayOutputStream generarOficioCredito(SolicitudCreditoEntity credito) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            Paragraph encabezado = new Paragraph("CAPSULABANK")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GREEN);
            document.add(encabezado);

            document.add(new Paragraph("OFICIO DE AUTORIZACION DE CREDITO")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" ").setHeight(15));

            document.add(new Paragraph("Oficio No.: CRED-" + String.format("%06d", credito.getId()))
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" ").setHeight(15));

            document.add(new Paragraph("Fecha: " + formatearFecha(credito.getFecha()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));
            document.add(new Paragraph(" ").setHeight(15));

            document.add(new Paragraph("ASUNTO: APROBACION DE CREDITO")
                .setBold()
                .setFontSize(12));
            document.add(new Paragraph(" ").setHeight(10));

            document.add(new Paragraph("Por medio del presente, CapsulaBank hace constar que se ha " +
                "APROBADO la solicitud de credito del cliente a continuacion descrito:")
                .setFontSize(10));
            document.add(new Paragraph(" ").setHeight(10));

            Table infoCredito = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoCredito.setWidth(UnitValue.createPercentValue(100));

            agregarFila(infoCredito, "Cliente:", credito.getUsuario().getNombre());
            agregarFila(infoCredito, "Usuario:", credito.getUsuario().getUsername());
            agregarFila(infoCredito, "Rol:", credito.getUsuario().getRol());
            agregarFila(infoCredito, "Monto Autorizado:", "$" + String.format("%,.2f", credito.getMontoSolicitado()));
            agregarFila(infoCredito, "Fecha Solicitud:", formatearFecha(credito.getFecha()));
            agregarFila(infoCredito, "Fecha Aprobacion:", LocalDateTime.now().format(FORMATTER));
            agregarFila(infoCredito, "Estado:", "APROBADO");

            document.add(infoCredito);

            document.add(new Paragraph(" ").setHeight(15));
            document.add(new Paragraph("Los terminos y condiciones aplican segun el contrato firmado por el cliente.")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph(" ").setHeight(15));
            document.add(new Paragraph("Atentamente,").setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph(" ").setHeight(5));
            document.add(new Paragraph("_________________________").setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("CapsulaBank - Direccion General")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph(" ").setHeight(20));
            document.add(new Paragraph("Este documento es de caracter informativo y no constituye un contrato legal.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando oficio de credito: " + e.getMessage());
        }
    }

    // ============================================================
    // REPORTE GENERAL DE CREDITOS - TODOS LOS CLIENTES
    // ============================================================
    public ByteArrayOutputStream generarReporteCreditosGeneral(List<SolicitudCreditoEntity> creditos) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            Paragraph titulo = new Paragraph("REPORTE GENERAL DE CREDITOS")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GREEN);
            document.add(titulo);

            document.add(new Paragraph("Fecha Reporte: " + LocalDateTime.now().format(FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));
            document.add(new Paragraph(" ").setHeight(10));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{8, 15, 12, 15, 12, 13}));
            tabla.setWidth(UnitValue.createPercentValue(100));

            agregarCeldaHeader(tabla, "ID");
            agregarCeldaHeader(tabla, "Cliente");
            agregarCeldaHeader(tabla, "Usuario");
            agregarCeldaHeader(tabla, "Monto");
            agregarCeldaHeader(tabla, "Estado");
            agregarCeldaHeader(tabla, "Fecha");

            for (SolicitudCreditoEntity c : creditos) {
                tabla.addCell(new Cell().add(new Paragraph(c.getId().toString())));
                tabla.addCell(new Cell().add(new Paragraph(c.getUsuario().getNombre())));
                tabla.addCell(new Cell().add(new Paragraph(c.getUsuario().getUsername())));
                tabla.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", c.getMontoSolicitado()))));
                tabla.addCell(new Cell().add(new Paragraph(c.getEstado())));
                tabla.addCell(new Cell().add(new Paragraph(formatearFecha(c.getFecha()))));
            }

            document.add(tabla);
            document.close();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando reporte general de creditos: " + e.getMessage());
        }
    }

    // ============================================================
    // REPORTE GENERAL DE MOVIMIENTOS - TODOS LOS CLIENTES
    // ============================================================
    public ByteArrayOutputStream generarReporteMovimientosGeneral(List<MovimientoEntity> movimientos) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            Paragraph titulo = new Paragraph("REPORTE GENERAL DE MOVIMIENTOS")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GREEN);
            document.add(titulo);

            document.add(new Paragraph("Fecha Reporte: " + LocalDateTime.now().format(FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));
            document.add(new Paragraph(" ").setHeight(10));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{8, 14, 14, 12, 12, 10, 12}));
            tabla.setWidth(UnitValue.createPercentValue(100));

            agregarCeldaHeader(tabla, "ID");
            agregarCeldaHeader(tabla, "Cta. Origen");
            agregarCeldaHeader(tabla, "Cta. Destino");
            agregarCeldaHeader(tabla, "Monto");
            agregarCeldaHeader(tabla, "Tipo");
            agregarCeldaHeader(tabla, "Estado");
            agregarCeldaHeader(tabla, "Fecha");

            for (MovimientoEntity m : movimientos) {
                tabla.addCell(new Cell().add(new Paragraph(m.getId().toString())));
                tabla.addCell(new Cell().add(new Paragraph(m.getCuentaOrigen() != null ? m.getCuentaOrigen() : "N/A")));
                tabla.addCell(new Cell().add(new Paragraph(m.getCuentaDestino() != null ? m.getCuentaDestino() : "N/A")));
                tabla.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", m.getMonto()))));
                tabla.addCell(new Cell().add(new Paragraph(m.getTipo() != null ? m.getTipo() : "N/A")));
                tabla.addCell(new Cell().add(new Paragraph(m.getEstadoMovimiento() != null ? m.getEstadoMovimiento() : "N/A")));
                tabla.addCell(new Cell().add(new Paragraph(formatearFecha(m.getFecha()))));
            }

            document.add(tabla);
            document.close();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando reporte general de movimientos: " + e.getMessage());
        }
    }

    private void agregarFila(Table tabla, String label, String valor) {
        tabla.addCell(new Cell().add(new Paragraph(label).setBold())
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph(valor))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private void agregarCeldaHeader(Table tabla, String texto) {
        tabla.addCell(new Cell().add(new Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(ColorConstants.DARK_GRAY));
    }
}
