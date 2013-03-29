package com.compomics.pride_asa_pipeline.logic.recalibration.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.recalibration.MassWindowFinder;
import java.util.List;

/**
 * A simple Class to find a window for values distrubuted around a given centre.
 *
 * This can be used to verify the mass accuracy of a masspec machine, where the
 * observed masses of ions vary according to the machines capabilities or
 * settings. Ideally the mass delta between theoretical and experimental mass
 * would be zero, but in reality the measurements differ slightly forming a
 * distribution around the correct mass.
 *
 * This class will start at the given centre and increase the window size in
 * iterative steps (of the defined step size) until the desired coverage or the
 * maximal window size is reached.
 *
 *
 * @author Florian Reisinger Date: 11-Aug-2009
 * @since 0.1
 */
public class SimpleMassWindowFinder implements MassWindowFinder {

    public double getStepSize() {
        return PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.step_size"); // default step size of 0.1 dalton
    }

    public void setStepSize(double stepSize) {
        PropertiesConfigurationHolder.getInstance().setProperty("masswindowfinder.step_size", stepSize);
    }

    public double getCoverage() {
        return PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.coverage"); // 100% coverage (all values have to be covered by the error window)
    }

    public void setCoverage(double coverage) {
        PropertiesConfigurationHolder.getInstance().setProperty("masswindowfinder.coverage", coverage);
    }

    public double getMaxWindow() {
        return PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.max_window");
    }

    @Override
    public void setMaxWindow(double maxWindow) {
        PropertiesConfigurationHolder.getInstance().setProperty("masswindowfinder.max_window", maxWindow);
    }

    public double getCentre() {
        return PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.centre");
    }

    @Override
    public void setCentre(double centre) {
        PropertiesConfigurationHolder.getInstance().setProperty("masswindowfinder.centre", centre);
    }

    /**
     * Find the window size necessary around the given centre to include the
     * values provided. Note: the algorithm can be adjusted to fit special
     * needs.
     *
     * @see #getStepSize()
     * @see #getCoverage()
     * @see #getMaxWindow()
     *
     * @param values the double values to find the window for.
     * @return the window size (in one direction) around the centre. (e.g. the
     * whole window = centre +/- window size.
     */
    @Override
    public double findMassWindow(List<Double> values) {
        double stepSize = PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.step_size");
        double coverage = PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.coverage");
        double maxWindow = PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.max_window");
        double centre = PropertiesConfigurationHolder.getInstance().getDouble("masswindowfinder.centre");

        double percentage = 0D; //percentage of values included in the current window
        int iteration = 0;      //number of times the window has been increased
        double windowSize = 0D; //the current window size (from the centre to one border)
        boolean stop = false;   //flag to stop the iteration
        while (percentage < coverage && !stop) {
            iteration++;
            if (((double) iteration * stepSize) < maxWindow) {
                //if we have not yet exceeded the allowed maximum window size
                windowSize = (double) iteration * stepSize;
            } else {
                //if we have reached the maximum window size, perform a 
                //last interation with that size, but stop afterwards
                windowSize = maxWindow;
                stop = true;
            }
            int included = 0;
            for (Double value : values) {
                //if the value is inside the window borders, count it as included
                if ((value > (centre - windowSize)) && (value < (centre + windowSize))) {
                    included++;
                }
            }
            //calculate the percentage of values included in the current window
            percentage = ((double) included / (double) values.size());
        }
        return windowSize;
    }
}
