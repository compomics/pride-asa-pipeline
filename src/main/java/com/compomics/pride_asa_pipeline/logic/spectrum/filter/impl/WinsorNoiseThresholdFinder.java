package com.compomics.pride_asa_pipeline.logic.spectrum.filter.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.spectrum.filter.NoiseThresholdFinder;
import com.compomics.pride_asa_pipeline.util.MathUtils;

/**
 * @author Florian Reisinger Date: 10-Sep-2009
 * @since 0.1
 */
public class WinsorNoiseThresholdFinder implements NoiseThresholdFinder {

    private double winsorisationConstant = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.constant");
    /**
     * the value to multiply the standard deviation with to define the outlier
     */
    private double outlierLimit = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.outlier_limit");
    private double convergenceCriterium = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.convergence_criterion");

    /**
     * Uses a winsonrisation approach (with the preset configuration) to find
     * the noise treshold for the privided signal values.
     *
     * @param signalValues list of signal values.
     * @return the calculated treshold separating noise from signal values.
     */
    @Override
    public double findNoiseThreshold(double[] signalValues) {
        //get winsorised peaks (where outlier spectrum intensities are redused to lower values)
        double[] intensities = winsorise(signalValues);

        double median = MathUtils.calculateMedian(intensities);
        double mean = MathUtils.calculateMean(intensities);
        double stdDev = Math.sqrt(MathUtils.calcVariance(intensities, mean));

        //calculate and return the noisetreshold for the spectrum (based on the winsorisation result)
        return median + outlierLimit * stdDev;
    }

    ///// ///// ///// ///// ////////// ///// ///// ///// /////
    // private methods
    private double[] winsorise(double[] signalValues) {
        double median = MathUtils.calculateMedian(signalValues);
        double currMAD = calcIntensityMAD(signalValues, median);
        double prevMAD = 3d * currMAD; //initial start value
        double[] correctedIntensities = new double[signalValues.length];

        while (((prevMAD - currMAD) / prevMAD) >= convergenceCriterium) {
            correctedIntensities = reduceOutliers(signalValues, median + (winsorisationConstant * currMAD));
            prevMAD = currMAD;
            currMAD = calcIntensityMAD(correctedIntensities, median);

            //ToDo: history reporting?
            //if we want to record the history, we can here capture the current treshold (possibly together with the iteration, if not: give e.g. a list)
            //double hypotheticalNoiseThreshold = median + OUTLIER_TH * currMAD;
        }

        return correctedIntensities;
    }

    private double calcIntensityMAD(double[] values, double median) {
        double[] diffs = new double[values.length];
        int cnt = 0;
        for (double p : values) {
            diffs[cnt++] = (Math.abs(p - median));
        }

        return MathUtils.calculateMedian(diffs);
    }

    private double[] reduceOutliers(double[] intensities, double maxIntensityLimit) {
        double[] correctedIntensities = new double[intensities.length];
        //sets all the values above the limit (outliers) to the limit
        //and therefore effectively eliminating outliers
        for (int i = 0; i < intensities.length; i++) {
            if (intensities[i] <= maxIntensityLimit) {
                correctedIntensities[i] = intensities[i];
            } else {
                correctedIntensities[i] = maxIntensityLimit;
            }
        }
        return correctedIntensities;
    }
}
