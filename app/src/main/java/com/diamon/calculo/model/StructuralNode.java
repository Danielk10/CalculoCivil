package com.diamon.calculo.model;

/**
 * Represents a structural node (joint) with coordinates and boundary conditions.
 */
public class StructuralNode {
    public int id;
    public double x, y, z;

    // Boundary conditions (true = fixed DOF)
    public boolean fixX, fixY, fixZ;
    public boolean fixRX, fixRY, fixRZ;

    // Displacement results
    public double dispX, dispY, dispZ;

    public StructuralNode() {}

    public StructuralNode(int id, double x, double y, double z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setFixed(boolean fx, boolean fy, boolean fz) {
        this.fixX = fx;
        this.fixY = fy;
        this.fixZ = fz;
    }

    public boolean isFullyFixed() {
        return fixX && fixY && fixZ;
    }

    public boolean isFree() {
        return !fixX && !fixY && !fixZ;
    }

    @Override
    public String toString() {
        return String.format("Node %d (%.2f, %.2f, %.2f) %s",
                id, x, y, z,
                isFullyFixed() ? "[FIXED]" : isFree() ? "[FREE]" : "[PARTIAL]");
    }
}
