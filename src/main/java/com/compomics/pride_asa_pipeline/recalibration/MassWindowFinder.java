package com.compomics.pride_asa_pipeline.recalibration;

import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 11-Aug-2009
 * @since 0.1
 */
public interface MassWindowFinder {
    
    /**
     * Sets the centre
     * 
     * @param centre the centre value
     */    
    public void setCentre(double centre);

    /**
     * Sets the maximium window size. 
     * The default value is retrieved from the properties file.
     * 
     * @param maxWindowSize the maximum window size value
     */
    public void setMaxWindow(double maxWindowSize);

    /**
     * Finds the window size necessary around the given centre to include
     * the values provided.
     * 
     * @param values the double values to find the window for.
     * @return the window size (in one direction) around the centre.
     *         (e.g. the whole window = centre +/- window size. 
     */
    public double findMassWindow(List<Double> values);

}
