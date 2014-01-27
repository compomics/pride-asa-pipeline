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
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.intensity) ^ (Double.doubleToLongBits(this.intensity) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.mz) ^ (Double.doubleToLongBits(this.mz) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Peak other = (Peak) obj;
        if (Double.doubleToLongBits(this.intensity) != Double.doubleToLongBits(other.intensity)) {
            return false;
        }
        if (Double.doubleToLongBits(this.mz) != Double.doubleToLongBits(other.mz)) {
            return false;
        }
        return true;
    }        

    @Override
    public int compareTo(Peak p) {
        return Double.compare(this.getIntensity(), p.getIntensity());
    }
}
