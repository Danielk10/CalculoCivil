package com.diamon.calculo.model;

/**
 * Represents a structural material with mechanical properties.
 */
public class StructuralMaterial {
    public int id;
    public String name;
    public String type; // "Concrete", "Steel", "Elastic"
    public double E;    // Elastic modulus (kPa)
    public double nu;   // Poisson's ratio
    public double fy;   // Yield strength (kPa)

    public StructuralMaterial() {}

    public StructuralMaterial(int id, String name, String type, double E, double nu, double fy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.E = E;
        this.nu = nu;
        this.fy = fy;
    }

    /** Concrete C30/35 preset */
    public static StructuralMaterial concreteC30(int id) {
        return new StructuralMaterial(id, "Concrete C30/35", "Concrete", 3.0e7, 0.2, 30000.0);
    }

    /** Steel A992 preset */
    public static StructuralMaterial steelA992(int id) {
        return new StructuralMaterial(id, "Steel A992", "Steel", 2.0e8, 0.3, 345000.0);
    }

    /** Steel A36 preset */
    public static StructuralMaterial steelA36(int id) {
        return new StructuralMaterial(id, "Steel A36", "Steel", 2.0e8, 0.3, 250000.0);
    }

    /** Generic elastic isotropic */
    public static StructuralMaterial elastic(int id, double E) {
        return new StructuralMaterial(id, "Elastic Isotropic", "Elastic", E, 0.3, 0.0);
    }

    @Override
    public String toString() {
        return String.format("Material %d: %s (E=%.2e, fy=%.2e, ν=%.3f)", id, name, E, fy, nu);
    }
}
