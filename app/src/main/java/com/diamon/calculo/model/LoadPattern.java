package com.diamon.calculo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a load pattern with associated node loads.
 */
public class LoadPattern {
    public int id;
    public String name;
    public String type; // "Dead", "Live", "EQX", "EQY", "ResponseSpectrum", "TimeHistory"
    public List<NodeLoad> loads;

    public LoadPattern() {
        this.loads = new ArrayList<>();
    }

    public LoadPattern(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.loads = new ArrayList<>();
    }

    public void addLoad(NodeLoad load) {
        if (this.loads == null) this.loads = new ArrayList<>();
        this.loads.add(load);
    }

    public void addLoad(int nodeId, double fx, double fy, double fz) {
        addLoad(new NodeLoad(nodeId, fx, fy, fz));
    }

    @Override
    public String toString() {
        return String.format("LoadPattern %d: %s (%s) - %d loads",
                id, name, type, loads != null ? loads.size() : 0);
    }
}
