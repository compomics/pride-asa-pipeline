package com.compomics.pride_asa_pipeline.model;

/**
 * @author Jonathan Rameseder
 * Date: 11-Jan-2008
 * @since 0.1
 */
public class Peak implements Comparable<Peak> {

    private double intensity;
    private double mz;

    public double getIntensity() {
        return intensity;
    }

    public double getMzRatio() {
        return mz;
    }

    public Peak(double mz, double intensity) {
        this.intensity = intensity;
        this.mz = mz;
    }

    @Override
    public int compareTo(Peak p) {
        return Double.compare(this.getIntensity(), p.getIntensity());
    }
}
