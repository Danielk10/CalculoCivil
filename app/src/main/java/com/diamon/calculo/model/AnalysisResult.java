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
}
