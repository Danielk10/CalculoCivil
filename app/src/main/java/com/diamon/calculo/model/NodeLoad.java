package com.diamon.calculo.model;

/**
 * Represents a concentrated load applied to a structural node.
 * Forces are in the global coordinate system.
 */
public class NodeLoad {
    public int nodeId;
    public double fx;  // Force in X direction (kN)
    public double fy;  // Force in Y direction (kN)
    public double fz;  // Force in Z direction (kN)
    public double mx;  // Moment about X axis (kN·m)
    public double my;  // Moment about Y axis (kN·m)
    public double mz;  // Moment about Z axis (kN·m)

    public NodeLoad() {}

    public NodeLoad(int nodeId, double fx, double fy, double fz) {
        this.nodeId = nodeId;
        this.fx = fx;
        this.fy = fy;
        this.fz = fz;
    }

    public NodeLoad(int nodeId, double fx, double fy, double fz,
                    double mx, double my, double mz) {
        this.nodeId = nodeId;
        this.fx = fx;
        this.fy = fy;
        this.fz = fz;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
    }

    @Override
    public String toString() {
        return String.format("NodeLoad{node=%d, F=(%.2f, %.2f, %.2f), M=(%.2f, %.2f, %.2f)}",
                nodeId, fx, fy, fz, mx, my, mz);
    }
}
