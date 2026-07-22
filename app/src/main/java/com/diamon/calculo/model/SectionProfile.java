package com.diamon.calculo.model;

/**
 * Represents a structural section cross-section profile.
 */
public class SectionProfile {
    public int id;
    public String name;
    public String type; // "IShape", "Rectangular", "Pipe", "Box"
    public double area;  // Cross-section area (m²)
    public double Iz;    // Moment of inertia about Z (m⁴)
    public double Iy;    // Moment of inertia about Y (m⁴)
    public double J;     // Torsional constant (m⁴)

    public SectionProfile() {}

    public SectionProfile(int id, String name, String type, double area, double Iz, double Iy, double J) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.area = area;
        this.Iz = Iz;
        this.Iy = Iy;
        this.J = J;
    }

    /** W-Beam W14x22 preset */
    public static SectionProfile w14x22(int id) {
        return new SectionProfile(id, "W14x22", "IShape",
                4.19e-3, 8.26e-5, 1.22e-5, 2.98e-7);
    }

    /** Rectangular concrete column 300x300mm */
    public static SectionProfile rectColumn300(int id) {
        return new SectionProfile(id, "RC 300x300", "Rectangular",
                0.09, 6.75e-4, 6.75e-4, 1.05e-3);
    }

    @Override
    public String toString() {
        return String.format("Section %d: %s (A=%.4e, Iz=%.4e)", id, name, area, Iz);
    }
}
