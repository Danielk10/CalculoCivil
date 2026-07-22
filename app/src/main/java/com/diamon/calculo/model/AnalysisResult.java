package com.diamon.calculo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for structural analysis results.
 */
public class AnalysisResult {
    private Map<Integer, double[]> nodeDisplacements;  // nodeId -> [Ux, Uy, Uz]
    private Map<Integer, double[]> elementForces;       // elemId -> [P, V22, V33, M22, M33, T]
    private List<Double> modalPeriods;                  // Natural periods T(s)
    private List<double[]> modeShapes;                  // Mode shape vectors
    private String analysisLog;

    public AnalysisResult() {
        this.nodeDisplacements = new HashMap<>();
        this.elementForces = new HashMap<>();
        this.modalPeriods = new ArrayList<>();
        this.modeShapes = new ArrayList<>();
        this.analysisLog = "";
    }

    public void addNodeDisplacement(int nodeId, double ux, double uy, double uz) {
        nodeDisplacements.put(nodeId, new double[]{ux, uy, uz});
    }

    public void addElementForces(int elemId, double P, double V22, double V33,
                                  double M22, double M33, double T) {
        elementForces.put(elemId, new double[]{P, V22, V33, M22, M33, T});
    }

    public void addModalPeriod(double period) {
        modalPeriods.add(period);
    }

    public void addModeShape(double[] shape) {
        modeShapes.add(shape);
    }

    public Map<Integer, double[]> getNodeDisplacements() { return nodeDisplacements; }
    public Map<Integer, double[]> getElementForces() { return elementForces; }
    public List<Double> getModalPeriods() { return modalPeriods; }
    public List<double[]> getModeShapes() { return modeShapes; }
    public String getAnalysisLog() { return analysisLog; }
    public void setAnalysisLog(String log) { this.analysisLog = log; }

    public void appendLog(String line) {
        if (this.analysisLog == null) this.analysisLog = "";
        this.analysisLog += line + "\n";
    }

    public static AnalysisResult parseOpenSeesOutput(String rawOutput) {
        AnalysisResult result = new AnalysisResult();
        result.setAnalysisLog(rawOutput);

        if (rawOutput == null || rawOutput.isEmpty()) return result;

        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("DISP ")) {
                try {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 3) {
                        int nodeId = Integer.parseInt(parts[1]);
                        double ux = Double.parseDouble(parts[2]);
                        double uy = parts.length > 3 ? Double.parseDouble(parts[3]) : 0.0;
                        double uz = parts.length > 4 ? Double.parseDouble(parts[4]) : 0.0;
                        result.addNodeDisplacement(nodeId, ux, uy, uz);
                    }
                } catch (Exception ignored) {}
            } else if (trimmed.startsWith("Node ") && (trimmed.contains("Ux=") || trimmed.contains("Uy="))) {
                try {
                    int nodeId = Integer.parseInt(trimmed.substring(5, trimmed.indexOf(":")).trim());
                    double ux = 0, uy = 0, uz = 0;
                    if (trimmed.contains("Ux=")) {
                        String s = trimmed.substring(trimmed.indexOf("Ux=") + 3).split("\\s+")[0].replaceAll("[^\n0-9eE.+-]", "");
                        ux = Double.parseDouble(s);
                    }
                    if (trimmed.contains("Uy=")) {
                        String s = trimmed.substring(trimmed.indexOf("Uy=") + 3).split("\\s+")[0].replaceAll("[^\n0-9eE.+-]", "");
                        uy = Double.parseDouble(s);
                    }
                    result.addNodeDisplacement(nodeId, ux, uy, uz);
                } catch (Exception ignored) {}
            } else if (trimmed.startsWith("FORCE ")) {
                try {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 3) {
                        int elemId = Integer.parseInt(parts[1]);
                        double p = Double.parseDouble(parts[2]);
                        double v22 = parts.length > 3 ? Double.parseDouble(parts[3]) : 0.0;
                        double v33 = parts.length > 4 ? Double.parseDouble(parts[4]) : 0.0;
                        double m22 = parts.length > 5 ? Double.parseDouble(parts[5]) : 0.0;
                        double m33 = parts.length > 6 ? Double.parseDouble(parts[6]) : 0.0;
                        double t = parts.length > 7 ? Double.parseDouble(parts[7]) : 0.0;
                        result.addElementForces(elemId, p, v22, v33, m22, m33, t);
                    }
                } catch (Exception ignored) {}
            }
        }
        return result;
    }

    public double getMaxDisplacement() {
        double maxDisp = 0.0;
        for (double[] d : nodeDisplacements.values()) {
            double mag = Math.sqrt(d[0] * d[0] + d[1] * d[1] + (d.length > 2 ? d[2] * d[2] : 0.0));
            if (mag > maxDisp) maxDisp = mag;
        }
        return maxDisp;
    }

    public double getMaxMoment() {
        double maxM = 0.0;
        for (double[] f : elementForces.values()) {
            double m = 0.0;
            if (f.length > 4) m = Math.abs(f[4]);
            else if (f.length > 2) m = Math.abs(f[2]);
            if (m > maxM) maxM = m;
        }
        return maxM;
    }
}
