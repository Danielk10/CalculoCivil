package com.diamon.calculo.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import com.diamon.calculo.model.AnalysisResult;
import com.diamon.calculo.model.FrameElement;
import com.diamon.calculo.model.LoadPattern;
import com.diamon.calculo.model.NodeLoad;
import com.diamon.calculo.model.SectionProfile;
import com.diamon.calculo.model.StructuralMaterial;
import com.diamon.calculo.model.StructuralModel;
import com.diamon.calculo.model.StructuralNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates professional A4 PDF structural analysis reports.
 * Uses android.graphics.pdf.PdfDocument for native PDF generation.
 */
public class PDFReportGenerator {
    private static final String TAG = "PDFReportGenerator";

    // A4 dimensions in PostScript points (72 dpi)
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final float MARGIN_LEFT = 40f;
    private static final float MARGIN_RIGHT = 40f;
    private static final float MARGIN_TOP = 50f;
    private static final float MARGIN_BOTTOM = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    // Paint objects
    private final Paint titlePaint;
    private final Paint headerPaint;
    private final Paint subHeaderPaint;
    private final Paint bodyPaint;
    private final Paint tablePaint;
    private final Paint tableHeaderPaint;
    private final Paint linePaint;
    private final Paint footerPaint;

    private int pageNumber = 0;

    public PDFReportGenerator() {
        // Title paint - 18sp bold
        titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(18f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setAntiAlias(true);

        // Section header paint - 14sp bold
        headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#1A237E")); // Dark blue
        headerPaint.setTextSize(14f);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setAntiAlias(true);

        // Sub-header paint - 12sp bold
        subHeaderPaint = new Paint();
        subHeaderPaint.setColor(Color.parseColor("#303F9F"));
        subHeaderPaint.setTextSize(12f);
        subHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subHeaderPaint.setAntiAlias(true);

        // Body text paint - 10sp
        bodyPaint = new Paint();
        bodyPaint.setColor(Color.DKGRAY);
        bodyPaint.setTextSize(10f);
        bodyPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        bodyPaint.setAntiAlias(true);

        // Table content paint - 9sp
        tablePaint = new Paint();
        tablePaint.setColor(Color.BLACK);
        tablePaint.setTextSize(9f);
        tablePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        tablePaint.setAntiAlias(true);

        // Table header paint - 9sp bold
        tableHeaderPaint = new Paint();
        tableHeaderPaint.setColor(Color.WHITE);
        tableHeaderPaint.setTextSize(9f);
        tableHeaderPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        tableHeaderPaint.setAntiAlias(true);

        // Line paint for table borders
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#9E9E9E"));
        linePaint.setStrokeWidth(0.5f);
        linePaint.setAntiAlias(true);

        // Footer paint
        footerPaint = new Paint();
        footerPaint.setColor(Color.GRAY);
        footerPaint.setTextSize(8f);
        footerPaint.setAntiAlias(true);
    }

    /**
     * Generate a complete structural analysis report PDF.
     */
    public boolean generateReport(Context context, StructuralModel model,
                                   String projectName, String engineerName,
                                   File outputFile) {
        PdfDocument document = new PdfDocument();
        pageNumber = 0;

        try {
            // Page 1: Cover / Header
            drawCoverPage(document, projectName, engineerName);

            // Page 2: Model Summary
            if (model != null) {
                drawModelSummaryPage(document, model);
            }

            // Page 3: Load Summary
            if (model != null && model.getLoadPatterns() != null && !model.getLoadPatterns().isEmpty()) {
                drawLoadSummaryPage(document, model);
            }

            // Page 4: Results
            if (model != null && model.getResult() != null) {
                drawResultsPage(document, model);
            }

            // Write to file
            FileOutputStream fos = new FileOutputStream(outputFile);
            document.writeTo(fos);
            fos.close();
            document.close();

            Log.i(TAG, "PDF report generated: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error generating PDF: " + e.getMessage());
            document.close();
            return false;
        }
    }

    private Canvas startPage(PdfDocument document) {
        pageNumber++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        return page.getCanvas();
    }

    private void finishPage(PdfDocument document, Canvas canvas) {
        // Draw footer
        String footer = String.format("Cálculo Civil v1.0 | OpenSees | Page %d", pageNumber);
        canvas.drawText(footer, MARGIN_LEFT, PAGE_HEIGHT - 20f, footerPaint);

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        float dateWidth = footerPaint.measureText(dateStr);
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN_RIGHT - dateWidth, PAGE_HEIGHT - 20f, footerPaint);

        // Draw footer line
        canvas.drawLine(MARGIN_LEFT, PAGE_HEIGHT - 35f,
                PAGE_WIDTH - MARGIN_RIGHT, PAGE_HEIGHT - 35f, linePaint);
    }

    // ========================= COVER PAGE =========================

    private void drawCoverPage(PdfDocument document, String projectName, String engineerName) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, ++pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float y = 120f;

        // Title block
        Paint bigTitle = new Paint(titlePaint);
        bigTitle.setTextSize(22f);
        String title = "REPORTE DE ANÁLISIS ESTRUCTURAL";
        float titleWidth = bigTitle.measureText(title);
        canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2f, y, bigTitle);
        y += 8f;

        // Underline
        Paint accentLine = new Paint();
        accentLine.setColor(Color.parseColor("#1A237E"));
        accentLine.setStrokeWidth(2f);
        canvas.drawLine((PAGE_WIDTH - titleWidth) / 2f, y,
                (PAGE_WIDTH + titleWidth) / 2f, y, accentLine);
        y += 40f;

        // Project info
        String[][] info = {
                {"Proyecto:", projectName != null ? projectName : "Cálculo Civil - Proyecto"},
                {"Ingeniero:", engineerName != null ? engineerName : "N/A"},
                {"Fecha:", new SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault()).format(new Date())},
                {"Software:", "Cálculo Civil v1.0"},
                {"Motor:", "OpenSees 3.8.0 (Pacific Earthquake Engineering Research Center)"},
                {"Plataforma:", "Android NDK / ARM64-v8a"}
        };

        Paint labelPaint = new Paint(headerPaint);
        labelPaint.setTextSize(12f);
        Paint valuePaint = new Paint(bodyPaint);
        valuePaint.setTextSize(12f);
        valuePaint.setTypeface(Typeface.DEFAULT);

        for (String[] row : info) {
            canvas.drawText(row[0], MARGIN_LEFT + 80f, y, labelPaint);
            canvas.drawText(row[1], MARGIN_LEFT + 200f, y, valuePaint);
            y += 22f;
        }

        y += 30f;

        // Divider
        canvas.drawLine(MARGIN_LEFT + 40f, y, PAGE_WIDTH - MARGIN_RIGHT - 40f, y, linePaint);
        y += 30f;

        // License notice
        Paint noticePaint = new Paint(bodyPaint);
        noticePaint.setTextSize(9f);
        noticePaint.setColor(Color.GRAY);
        String[] notice = {
                "Cálculo Civil es un desarrollo independiente para análisis y cálculo estructural.",
                "Motor de análisis: OpenSees (PEER, University of California, Berkeley).",
                "",
                "Copyright (c) 1999-2024 The Regents of the University of California.",
                "Todos los derechos reservados. Licencia BSD.",
                "",
                "DISCLAIMER: Este informe se genera con fines de cálculo y referencia.",
                "El ingeniero proyectista/calculista es responsable de verificar todos los resultados."
        };

        for (String line : notice) {
            float lineWidth = noticePaint.measureText(line);
            canvas.drawText(line, (PAGE_WIDTH - lineWidth) / 2f, y, noticePaint);
            y += 14f;
        }

        finishPage(document, canvas);
        document.finishPage(page);
    }

    // ========================= MODEL SUMMARY PAGE =========================

    private void drawModelSummaryPage(PdfDocument document, StructuralModel model) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, ++pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float y = MARGIN_TOP;

        // Section title
        canvas.drawText("MODEL SUMMARY", MARGIN_LEFT, y, headerPaint);
        y += 6f;
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint);
        y += 20f;

        // Node table
        List<StructuralNode> nodes = model.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            canvas.drawText("Node Coordinates & Boundary Conditions", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] nodeHeaders = {"ID", "X (m)", "Y (m)", "Z (m)", "Fix X", "Fix Y", "Fix Z"};
            float[] nodeColWidths = {40f, 70f, 70f, 70f, 55f, 55f, 55f};

            y = drawTableHeader(canvas, nodeHeaders, nodeColWidths, y);

            for (StructuralNode node : nodes) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break; // Page overflow guard
                String[] row = {
                        String.valueOf(node.id),
                        String.format(Locale.US, "%.3f", node.x),
                        String.format(Locale.US, "%.3f", node.y),
                        String.format(Locale.US, "%.3f", node.z),
                        node.fixX ? "Yes" : "No",
                        node.fixY ? "Yes" : "No",
                        node.fixZ ? "Yes" : "No"
                };
                y = drawTableRow(canvas, row, nodeColWidths, y);
            }
            y += 20f;
        }

        // Element table
        List<FrameElement> elements = model.getElements();
        if (elements != null && !elements.isEmpty()) {
            canvas.drawText("Element Connectivity", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] elemHeaders = {"ID", "Node I", "Node J", "Material", "Section", "Transf."};
            float[] elemColWidths = {40f, 65f, 65f, 80f, 80f, 80f};

            y = drawTableHeader(canvas, elemHeaders, elemColWidths, y);

            for (FrameElement elem : elements) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                String[] row = {
                        String.valueOf(elem.id),
                        String.valueOf(elem.nodeI),
                        String.valueOf(elem.nodeJ),
                        String.valueOf(elem.materialId),
                        String.valueOf(elem.sectionId),
                        elem.geomTransfType != null ? elem.geomTransfType : "Linear"
                };
                y = drawTableRow(canvas, row, elemColWidths, y);
            }
            y += 20f;
        }

        // Material table
        List<StructuralMaterial> materials = model.getMaterials();
        if (materials != null && !materials.isEmpty()) {
            canvas.drawText("Material Properties", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] matHeaders = {"ID", "Name", "E (kPa)", "fy (kPa)", "ν"};
            float[] matColWidths = {40f, 100f, 100f, 100f, 70f};

            y = drawTableHeader(canvas, matHeaders, matColWidths, y);

            for (StructuralMaterial mat : materials) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                String[] row = {
                        String.valueOf(mat.id),
                        mat.name != null ? mat.name : "N/A",
                        String.format(Locale.US, "%.2e", mat.E),
                        String.format(Locale.US, "%.2e", mat.fy),
                        String.format(Locale.US, "%.3f", mat.nu)
                };
                y = drawTableRow(canvas, row, matColWidths, y);
            }
            y += 20f;
        }

        // Section table
        List<SectionProfile> sections = model.getSections();
        if (sections != null && !sections.isEmpty()) {
            canvas.drawText("Section Properties", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] secHeaders = {"ID", "Name", "A (m²)", "Iz (m⁴)", "Iy (m⁴)", "J (m⁴)"};
            float[] secColWidths = {40f, 90f, 80f, 80f, 80f, 80f};

            y = drawTableHeader(canvas, secHeaders, secColWidths, y);

            for (SectionProfile sec : sections) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                String[] row = {
                        String.valueOf(sec.id),
                        sec.name != null ? sec.name : "N/A",
                        String.format(Locale.US, "%.4e", sec.area),
                        String.format(Locale.US, "%.4e", sec.Iz),
                        String.format(Locale.US, "%.4e", sec.Iy),
                        String.format(Locale.US, "%.4e", sec.J)
                };
                y = drawTableRow(canvas, row, secColWidths, y);
            }
        }

        finishPage(document, canvas);
        document.finishPage(page);
    }

    // ========================= LOAD SUMMARY PAGE =========================

    private void drawLoadSummaryPage(PdfDocument document, StructuralModel model) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, ++pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float y = MARGIN_TOP;

        canvas.drawText("LOAD PATTERNS", MARGIN_LEFT, y, headerPaint);
        y += 6f;
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint);
        y += 20f;

        for (LoadPattern lp : model.getLoadPatterns()) {
            if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 60f) break;

            canvas.drawText(String.format("Pattern %d: %s (%s)",
                    lp.id, lp.name != null ? lp.name : "N/A",
                    lp.type != null ? lp.type : "N/A"), MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            if (lp.loads != null && !lp.loads.isEmpty()) {
                String[] loadHeaders = {"Node", "Fx (kN)", "Fy (kN)", "Fz (kN)", "Mx (kN·m)", "My (kN·m)", "Mz (kN·m)"};
                float[] loadColWidths = {45f, 65f, 65f, 65f, 65f, 65f, 65f};

                y = drawTableHeader(canvas, loadHeaders, loadColWidths, y);

                for (NodeLoad nl : lp.loads) {
                    if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                    String[] row = {
                            String.valueOf(nl.nodeId),
                            String.format(Locale.US, "%.2f", nl.fx),
                            String.format(Locale.US, "%.2f", nl.fy),
                            String.format(Locale.US, "%.2f", nl.fz),
                            String.format(Locale.US, "%.2f", nl.mx),
                            String.format(Locale.US, "%.2f", nl.my),
                            String.format(Locale.US, "%.2f", nl.mz)
                    };
                    y = drawTableRow(canvas, row, loadColWidths, y);
                }
                y += 16f;
            }
        }

        finishPage(document, canvas);
        document.finishPage(page);
    }

    // ========================= RESULTS PAGE =========================

    private void drawResultsPage(PdfDocument document, StructuralModel model) {
        AnalysisResult result = model.getResult();
        if (result == null) return;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, ++pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float y = MARGIN_TOP;

        canvas.drawText("ANALYSIS RESULTS", MARGIN_LEFT, y, headerPaint);
        y += 6f;
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint);
        y += 20f;

        // Nodal Displacements
        Map<Integer, double[]> displacements = result.getNodeDisplacements();
        if (displacements != null && !displacements.isEmpty()) {
            canvas.drawText("Nodal Displacements", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] dispHeaders = {"Node ID", "Ux (m)", "Uy (m)", "Uz (m)"};
            float[] dispColWidths = {60f, 120f, 120f, 120f};

            y = drawTableHeader(canvas, dispHeaders, dispColWidths, y);

            for (Map.Entry<Integer, double[]> entry : displacements.entrySet()) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                double[] disp = entry.getValue();
                String[] row = {
                        String.valueOf(entry.getKey()),
                        String.format(Locale.US, "%.6e", disp.length > 0 ? disp[0] : 0.0),
                        String.format(Locale.US, "%.6e", disp.length > 1 ? disp[1] : 0.0),
                        String.format(Locale.US, "%.6e", disp.length > 2 ? disp[2] : 0.0)
                };
                y = drawTableRow(canvas, row, dispColWidths, y);
            }
            y += 20f;
        }

        // Element Forces
        Map<Integer, double[]> forces = result.getElementForces();
        if (forces != null && !forces.isEmpty()) {
            canvas.drawText("Element Internal Forces", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] forceHeaders = {"Elem ID", "P (kN)", "V22 (kN)", "V33 (kN)", "M22 (kN·m)", "M33 (kN·m)", "T (kN·m)"};
            float[] forceColWidths = {50f, 65f, 65f, 65f, 65f, 65f, 60f};

            y = drawTableHeader(canvas, forceHeaders, forceColWidths, y);

            for (Map.Entry<Integer, double[]> entry : forces.entrySet()) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                double[] f = entry.getValue();
                String[] row = {
                        String.valueOf(entry.getKey()),
                        String.format(Locale.US, "%.3f", f.length > 0 ? f[0] : 0.0),
                        String.format(Locale.US, "%.3f", f.length > 1 ? f[1] : 0.0),
                        String.format(Locale.US, "%.3f", f.length > 2 ? f[2] : 0.0),
                        String.format(Locale.US, "%.3f", f.length > 3 ? f[3] : 0.0),
                        String.format(Locale.US, "%.3f", f.length > 4 ? f[4] : 0.0),
                        String.format(Locale.US, "%.3f", f.length > 5 ? f[5] : 0.0)
                };
                y = drawTableRow(canvas, row, forceColWidths, y);
            }
            y += 20f;
        }

        // Modal Periods
        List<Double> periods = result.getModalPeriods();
        if (periods != null && !periods.isEmpty()) {
            canvas.drawText("Modal Analysis - Natural Periods", MARGIN_LEFT, y, subHeaderPaint);
            y += 16f;

            String[] modalHeaders = {"Mode", "Period T (s)", "Frequency f (Hz)", "ω (rad/s)"};
            float[] modalColWidths = {60f, 110f, 110f, 110f};

            y = drawTableHeader(canvas, modalHeaders, modalColWidths, y);

            for (int i = 0; i < periods.size(); i++) {
                if (y > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) break;
                double T = periods.get(i);
                double freq = T > 0 ? 1.0 / T : 0.0;
                double omega = T > 0 ? 2.0 * Math.PI / T : 0.0;
                String[] row = {
                        String.valueOf(i + 1),
                        String.format(Locale.US, "%.6f", T),
                        String.format(Locale.US, "%.4f", freq),
                        String.format(Locale.US, "%.4f", omega)
                };
                y = drawTableRow(canvas, row, modalColWidths, y);
            }
        }

        finishPage(document, canvas);
        document.finishPage(page);
    }

    // ========================= TABLE DRAWING HELPERS =========================

    /**
     * Draw a table header row with dark blue background.
     */
    private float drawTableHeader(Canvas canvas, String[] headers, float[] colWidths, float y) {
        float rowHeight = 16f;
        float x = MARGIN_LEFT;

        // Header background
        Paint headerBg = new Paint();
        headerBg.setColor(Color.parseColor("#1A237E"));
        canvas.drawRect(x, y - 11f, x + sumArray(colWidths), y + rowHeight - 7f, headerBg);

        // Header text
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], x + 4f, y + 2f, tableHeaderPaint);
            x += colWidths[i];
        }

        return y + rowHeight;
    }

    /**
     * Draw a table data row with alternating background.
     */
    private float drawTableRow(Canvas canvas, String[] values, float[] colWidths, float y) {
        float rowHeight = 14f;
        float x = MARGIN_LEFT;

        // Alternating row background
        Paint rowBg = new Paint();
        int rowIndex = (int) ((y - MARGIN_TOP) / rowHeight);
        rowBg.setColor(rowIndex % 2 == 0 ? Color.parseColor("#F5F5F5") : Color.WHITE);
        canvas.drawRect(x, y - 10f, x + sumArray(colWidths), y + rowHeight - 8f, rowBg);

        // Row border
        canvas.drawLine(x, y + rowHeight - 8f,
                x + sumArray(colWidths), y + rowHeight - 8f, linePaint);

        // Cell text
        for (int i = 0; i < values.length && i < colWidths.length; i++) {
            String text = values[i];
            if (text == null) text = "N/A";
            // Truncate if too wide
            float maxWidth = colWidths[i] - 8f;
            if (tablePaint.measureText(text) > maxWidth) {
                while (text.length() > 1 && tablePaint.measureText(text + "…") > maxWidth) {
                    text = text.substring(0, text.length() - 1);
                }
                text += "…";
            }
            canvas.drawText(text, x + 4f, y + 2f, tablePaint);
            x += colWidths[i];
        }

        return y + rowHeight;
    }

    private float sumArray(float[] arr) {
        float sum = 0;
        for (float v : arr) sum += v;
        return sum;
    }
}
