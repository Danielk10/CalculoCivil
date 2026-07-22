package com.diamon.calculo.model;

/**
 * Represents a frame (beam/column) element connecting two nodes.
 */
public class FrameElement {
    public int id;
    public int nodeI;
    public int nodeJ;
    public int materialId;
    public int sectionId;
    public String geomTransfType; // "Linear", "PDelta", "Corotational"

    public FrameElement() {
        this.geomTransfType = "Linear";
    }

    public FrameElement(int id, int nodeI, int nodeJ, int materialId, int sectionId) {
        this.id = id;
        this.nodeI = nodeI;
        this.nodeJ = nodeJ;
        this.materialId = materialId;
        this.sectionId = sectionId;
        this.geomTransfType = "Linear";
    }

    @Override
    public String toString() {
        return String.format("Element %d: Node %d -> Node %d (Mat:%d, Sec:%d, %s)",
                id, nodeI, nodeJ, materialId, sectionId, geomTransfType);
    }
}
